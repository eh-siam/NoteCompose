package com.example.notecompose.data.data_source

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.notecompose.domain.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val title: String,
    val content: String,
    val timestamp: Long,
    val category: String = "All",
    val isBookmarked: Boolean = false,
    val pin: String? = null,
    val userId: String // Added userId to separate notes by user
) {
    fun toNote(): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            timestamp = timestamp,
            category = category,
            isBookmarked = isBookmarked,
            pin = pin,
            userId = userId
        )
    }

    companion object {
        fun fromNote(note: Note): NoteEntity {
            return NoteEntity(
                id = note.id,
                title = note.title,
                content = note.content,
                timestamp = note.timestamp,
                category = note.category,
                isBookmarked = note.isBookmarked,
                pin = note.pin,
                userId = note.userId
            )
        }
    }
}
