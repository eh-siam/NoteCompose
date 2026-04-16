package com.example.notecompose.domain.use_case

import com.example.notecompose.domain.model.Note
import com.example.notecompose.domain.repository.NoteRepository

class DeleteNote(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.deleteNote(note)
    }
}
