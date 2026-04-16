package com.example.notecompose.domain.use_case

import com.example.notecompose.domain.model.Note
import com.example.notecompose.domain.repository.NoteRepository
import com.example.notecompose.domain.util.NoteOrder
import com.example.notecompose.domain.util.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use Case to retrieve notes from the repository.
 * It also handles business logic like sorting.
 */
class GetNotes(
    private val repository: NoteRepository
) {
    operator fun invoke(
        userId: String,
        noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending)
    ): Flow<List<Note>> {
        return repository.getNotes(userId).map { notes ->
            when(noteOrder.orderType) {
                is OrderType.Ascending -> {
                    when(noteOrder) {
                        is NoteOrder.Title -> notes.sortedBy { it.title.lowercase() }
                        is NoteOrder.Date -> notes.sortedBy { it.timestamp }
                    }
                }
                is OrderType.Descending -> {
                    when(noteOrder) {
                        is NoteOrder.Title -> notes.sortedByDescending { it.title.lowercase() }
                        is NoteOrder.Date -> notes.sortedByDescending { it.timestamp }
                    }
                }
            }
        }
    }
}
