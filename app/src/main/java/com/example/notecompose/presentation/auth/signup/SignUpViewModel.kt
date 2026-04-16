package com.example.notecompose.presentation.auth.signup

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
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onPasswordChange(password: String) {
        _password.value = password
    }

    fun signUp() {
        viewModelScope.launch {
            val email = _email.value.trim()
            val password = _password.value.trim()
            
            if (email.isBlank() || password.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Email or password cannot be empty"))
                return@launch
            }
            _isLoading.value = true
            try {
                authRepository.signUp(email, password)
                _eventFlow.emit(UiEvent.SignUpSuccess)
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar(e.localizedMessage ?: "An error occurred"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object SignUpSuccess : UiEvent()
    }
}
