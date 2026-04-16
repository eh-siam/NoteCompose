package com.example.notecompose.presentation.auth.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecompose.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EnteredEmail -> {
                _state.value = _state.value.copy(email = event.value)
            }
            is LoginEvent.EnteredPassword -> {
                _state.value = _state.value.copy(password = event.value)
            }
            is LoginEvent.TogglePasswordVisibility -> {
                _state.value = _state.value.copy(isPasswordVisible = !_state.value.isPasswordVisible)
            }
            is LoginEvent.Login -> {
                login()
            }
            is LoginEvent.ForgotPassword -> {
                forgotPassword()
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            val email = _state.value.email.trim()
            val password = _state.value.password.trim()

            if (email.isBlank() || password.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Email or password cannot be empty"))
                return@launch
            }
            _state.value = _state.value.copy(isLoading = true)
            try {
                authRepository.login(email, password)
                _eventFlow.emit(UiEvent.LoginSuccess)
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar(e.localizedMessage ?: "An error occurred"))
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun forgotPassword() {
        viewModelScope.launch {
            val email = _state.value.email.trim()
            if (email.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Please enter your email to reset password"))
                return@launch
            }
            _state.value = _state.value.copy(isLoading = true)
            try {
                authRepository.sendPasswordResetEmail(email)
                _eventFlow.emit(UiEvent.ShowSnackbar("Password reset email sent"))
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar(e.localizedMessage ?: "An error occurred"))
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object LoginSuccess : UiEvent()
    }
}
