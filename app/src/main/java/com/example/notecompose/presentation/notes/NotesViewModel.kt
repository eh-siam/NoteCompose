package com.example.notecompose.presentation.notes

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecompose.domain.model.Note
import com.example.notecompose.domain.repository.NoteRepository
import com.example.notecompose.domain.use_case.NoteUseCases
import com.example.notecompose.domain.util.NoteOrder
import com.example.notecompose.domain.util.OrderType
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Notes list screen.
 * It communicates with UseCases and exposes State to the UI.
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases,
    private val repository: NoteRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = mutableStateOf(NotesState())
    val state: State<NotesState> = _state

    private var recentlyDeletedNote: Note? = null
    private var getNotesJob: Job? = null
    private var allNotes: List<Note> = emptyList()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    init {
        currentUserId?.let {
            getNotes(it, NoteOrder.Date(OrderType.Descending))
        }
    }

    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.Order -> {
                if (state.value.noteOrder::class == event.noteOrder::class &&
                    state.value.noteOrder.orderType == event.noteOrder.orderType
                ) {
                    return
                }
                currentUserId?.let { getNotes(it, event.noteOrder) }
            }
            is NotesEvent.DeleteNote -> {
                viewModelScope.launch {
                    noteUseCases.deleteNote(event.note)
                    recentlyDeletedNote = event.note
                }
            }
            is NotesEvent.RestoreNote -> {
                viewModelScope.launch {
                    noteUseCases.addNote(recentlyDeletedNote ?: return@launch)
                    recentlyDeletedNote = null
                }
            }
            is NotesEvent.ToggleOrderSection -> {
                _state.value = state.value.copy(
                    isOrderSectionVisible = !state.value.isOrderSectionVisible
                )
            }
            is NotesEvent.ChangeCategory -> {
                _state.value = state.value.copy(
                    selectedCategory = event.category,
                    notes = if (event.category == "All") {
                        allNotes
                    } else {
                        allNotes.filter { it.category == event.category }
                    }
                )
            }
            is NotesEvent.ToggleBookmark -> {
                viewModelScope.launch {
                    noteUseCases.addNote(
                        event.note.copy(isBookmarked = !event.note.isBookmarked)
                    )
                }
            }
        }
    }

    private fun getNotes(userId: String, noteOrder: NoteOrder) {
        getNotesJob?.cancel()
        getNotesJob = noteUseCases.getNotes(userId, noteOrder)
            .onEach { notes ->
                allNotes = notes
                _state.value = state.value.copy(
                    notes = if (state.value.selectedCategory == "All") {
                        notes
                    } else {
                        notes.filter { it.category == state.value.selectedCategory }
                    },
                    noteOrder = noteOrder
                )
            }
            .launchIn(viewModelScope)
    }
}
