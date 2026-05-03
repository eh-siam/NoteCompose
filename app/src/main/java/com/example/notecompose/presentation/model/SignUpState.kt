package com.example.notecompose.presentation.model

data class SignUpState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)
