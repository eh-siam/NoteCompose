package com.example.notecompose.presentation.add_edit_note

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notecompose.domain.model.InvalidNoteException
import com.example.notecompose.domain.model.Note
import com.example.notecompose.domain.use_case.NoteUseCases
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _noteTitle = mutableStateOf(AddEditNoteState(
        isHintVisible = true
    ))
    val noteTitle: State<AddEditNoteState> = _noteTitle

    private val _noteContent = mutableStateOf(AddEditNoteState(
        isHintVisible = true
    ))
    val noteContent: State<AddEditNoteState> = _noteContent

    private val _noteCategory = mutableStateOf("All")
    val noteCategory: State<String> = _noteCategory

    private val _notePin = mutableStateOf<String?>(null)
    val notePin: State<String?> = _notePin

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    var currentNoteId: Int? = null

    init {
        savedStateHandle.get<Int>("noteId")?.let { noteId ->
            if(noteId != -1) {
                viewModelScope.launch {
                    noteUseCases.getNote(noteId)?.also { note ->
                        currentNoteId = note.id
                        _noteTitle.value = noteTitle.value.copy(
                            title = note.title,
                            isHintVisible = false
                        )
                        _noteContent.value = noteContent.value.copy(
                            content = note.content,
                            isHintVisible = false
                        )
                        _noteCategory.value = note.category
                        _notePin.value = note.pin
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditNoteEvent) {
        when(event) {
            is AddEditNoteEvent.EnteredTitle -> {
                _noteTitle.value = noteTitle.value.copy(
                    title = event.value
                )
            }
            is AddEditNoteEvent.ChangeTitleFocus -> {
                _noteTitle.value = noteTitle.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            noteTitle.value.title.isBlank()
                )
            }
            is AddEditNoteEvent.EnteredContent -> {
                _noteContent.value = noteContent.value.copy(
                    content = event.value
                )
            }
            is AddEditNoteEvent.ChangeContentFocus -> {
                _noteContent.value = noteContent.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            noteContent.value.content.isBlank()
                )
            }
            is AddEditNoteEvent.ChangeCategory -> {
                _noteCategory.value = event.category
            }
            is AddEditNoteEvent.EnteredPin -> {
                _notePin.value = event.value
            }
            is AddEditNoteEvent.SaveNote -> {
                viewModelScope.launch {
                    try {
                        noteUseCases.addNote(
                            Note(
                                title = noteTitle.value.title,
                                content = noteContent.value.content,
                                timestamp = System.currentTimeMillis(),
                                category = noteCategory.value,
                                id = currentNoteId,
                                pin = notePin.value,
                                userId = auth.currentUser?.uid ?: ""
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveNote)
                    } catch(e: InvalidNoteException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save note"
                            )
                        )
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String): UiEvent()
        object SaveNote: UiEvent()
    }
}
