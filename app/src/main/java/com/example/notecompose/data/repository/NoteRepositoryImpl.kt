package com.example.notecompose.data.repository

import android.util.Log
import com.example.notecompose.data.data_source.NoteDao
import com.example.notecompose.data.data_source.NoteEntity
import com.example.notecompose.domain.model.Note
import com.example.notecompose.domain.repository.NoteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NoteRepositoryImpl(
    private val dao: NoteDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : NoteRepository {

    override fun getNotes(userId: String): Flow<List<Note>> {
        return dao.getNotes(userId).map { entities ->
            entities.map { it.toNote() }
        }
    }

    override suspend fun getNoteById(id: Int): Note? {
        return dao.getNoteById(id)?.toNote()
    }

    override suspend fun insertNote(note: Note) {
        dao.insertNote(NoteEntity.fromNote(note))
        
        val uid = note.userId
        val noteMap = hashMapOf(
            "title" to note.title,
            "content" to note.content,
            "timestamp" to note.timestamp,
            "category" to note.category,
            "isBookmarked" to note.isBookmarked,
            "userId" to note.userId,
            "pin" to note.pin
        )
        try {
            firestore.collection("users")
                .document(uid)
                .collection("notes")
                .document(note.title)
                .set(noteMap)
                .await()
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error syncing with Firestore: ${e.message}")
        }
    }

    override suspend fun deleteNote(note: Note) {
        dao.deleteNote(NoteEntity.fromNote(note))
        
        val uid = note.userId
        try {
            firestore.collection("users")
                .document(uid)
                .collection("notes")
                .document(note.title)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error deleting from Firestore: ${e.message}")
        }
    }

    override suspend fun syncWithFirestore(userId: String) {
        try {
            val result = firestore.collection("users")
                .document(userId)
                .collection("notes")
                .get()
                .await()
            
            result.documents.forEach { doc ->
                val title = doc.getString("title") ?: return@forEach
                val content = doc.getString("content") ?: ""
                val timestamp = doc.getLong("timestamp") ?: 0L
                val category = doc.getString("category") ?: "All"
                val isBookmarked = doc.getBoolean("isBookmarked") ?: false
                val pin = doc.getString("pin")
                
                val existingNote = dao.getNoteByTitle(title, userId)
                val noteEntity = NoteEntity(
                    id = existingNote?.id, // Use existing ID to avoid duplicates
                    title = title,
                    content = content,
                    timestamp = timestamp,
                    category = category,
                    isBookmarked = isBookmarked,
                    pin = pin,
                    userId = userId
                )
                dao.insertNote(noteEntity)
            }
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error fetching from Firestore: ${e.message}")
        }
    }
}
