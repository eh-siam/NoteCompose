package com.example.notecompose.presentation.model

sealed class SignUpEvent {
    data class EnteredEmail(val value: String): SignUpEvent()
    data class EnteredPassword(val value: String): SignUpEvent()
    object SignUp: SignUpEvent()
}
