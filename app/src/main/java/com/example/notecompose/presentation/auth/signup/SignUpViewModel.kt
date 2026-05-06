package com.example.notecompose.presentation.auth.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecompose.domain.repository.AuthRepository
import com.example.notecompose.presentation.model.SignUpEvent
import com.example.notecompose.presentation.model.SignUpState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = mutableStateOf(SignUpState())
    val state: State<SignUpState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.EnteredEmail -> {
                _state.value = _state.value.copy(email = event.value)
            }
            is SignUpEvent.EnteredPassword -> {
                _state.value = _state.value.copy(password = event.value)
            }
            is SignUpEvent.SignUp -> {
                signUp()
            }
        }
    }

    private fun signUp() {
        viewModelScope.launch {
            val email = _state.value.email.trim()
            val password = _state.value.password.trim()
            
            if (email.isBlank() || password.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Email or password cannot be empty"))
                return@launch
            }
            _state.value = _state.value.copy(isLoading = true)
            try {
                authRepository.signUp(email, password)
                _eventFlow.emit(UiEvent.SignUpSuccess)
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar(e.localizedMessage ?: "An error occurred"))
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object SignUpSuccess : UiEvent()
    }
}
