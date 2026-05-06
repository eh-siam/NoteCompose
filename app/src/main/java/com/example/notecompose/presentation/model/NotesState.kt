package com.example.notecompose.presentation.model

import com.example.notecompose.domain.model.Note
import com.example.notecompose.domain.util.NoteOrder
import com.example.notecompose.domain.util.OrderType

data class NotesState(
    val notes: List<Note> = emptyList(),
    val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false,
    val selectedCategory: String = "All"
)
