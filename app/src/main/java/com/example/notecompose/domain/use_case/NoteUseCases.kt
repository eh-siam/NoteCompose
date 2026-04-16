package com.example.notecompose.domain.use_case

/**
 * Wrapper class to inject all use cases into the ViewModel.
 */
data class NoteUseCases(
    val getNotes: GetNotes,
    val deleteNote: DeleteNote,
    val addNote: AddNote,
    val getNote: GetNote
)
