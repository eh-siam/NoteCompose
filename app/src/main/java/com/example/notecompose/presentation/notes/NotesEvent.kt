package com.example.notecompose.presentation.notes

import com.example.notecompose.domain.model.Note
import com.example.notecompose.domain.util.NoteOrder

/**
 * Sealed class representing all possible user actions (events) 
 * on the Notes list screen.
 */
sealed class NotesEvent {
    data class Order(val noteOrder: NoteOrder): NotesEvent()
    data class DeleteNote(val note: Note): NotesEvent()
    object RestoreNote: NotesEvent()
    object ToggleOrderSection: NotesEvent()
    data class ChangeCategory(val category: String): NotesEvent()
    data class ToggleBookmark(val note: Note): NotesEvent()
    data class FinishNote(val note: Note): NotesEvent()
}
