package com.example.notecompose.presentation.model

sealed class LoginEvent {
    data class EnteredEmail(val value: String): LoginEvent()
    data class EnteredPassword(val value: String): LoginEvent()
    object TogglePasswordVisibility: LoginEvent()
    object Login: LoginEvent()
    object ForgotPassword: LoginEvent()
}
