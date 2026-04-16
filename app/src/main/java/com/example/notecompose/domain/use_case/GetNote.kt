package com.example.notecompose.domain.use_case

import com.example.notecompose.domain.model.Note
import com.example.notecompose.domain.repository.NoteRepository

class GetNote(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: Int): Note? {
        return repository.getNoteById(id)
    }
}
