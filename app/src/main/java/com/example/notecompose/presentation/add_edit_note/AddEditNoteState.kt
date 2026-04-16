package com.example.notecompose.presentation.add_edit_note

data class AddEditNoteState(
    val title: String = "",
    val content: String = "",
    val isHintVisible: Boolean = true,
    val pin: String? = null
)
