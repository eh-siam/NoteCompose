package com.example.notecompose.presentation.model

data class AddEditNoteState(
    val title: String = "",
    val content: String = "",
    val isHintVisible: Boolean = true,
    val pin: String? = null
)
