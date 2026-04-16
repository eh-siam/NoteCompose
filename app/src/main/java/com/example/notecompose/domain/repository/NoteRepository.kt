package com.example.notecompose.domain.repository

import com.example.notecompose.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(userId: String): Flow<List<Note>>
    suspend fun getNoteById(id: Int): Note?
    suspend fun insertNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun syncWithFirestore(userId: String)
}
