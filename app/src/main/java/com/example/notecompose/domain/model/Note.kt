package com.example.notecompose.domain.model

/**
 * Domain model for a Note.
 * This is the single source of truth for a note in the business logic.
 */
data class Note(
    val id: Int? = null,
    val title: String,
    val content: String,
    val timestamp: Long,
    val category: String = "All",
    val isBookmarked: Boolean = false,
    val pin: String? = null,
    val userId: String
) {
    companion object {
        val categories = listOf("All", "Work", "Birthday",  "Occasions","Important")
    }
}

class InvalidNoteException(message: String): Exception(message)
