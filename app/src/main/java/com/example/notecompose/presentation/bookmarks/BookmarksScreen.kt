package com.example.notecompose.presentation.bookmarks

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.notecompose.domain.model.Note
import com.example.notecompose.presentation.notes.NotesEvent
import com.example.notecompose.presentation.notes.NotesViewModel
import com.example.notecompose.presentation.notes.components.NoteItem
import com.example.notecompose.presentation.util.BottomNavigationBar
import com.example.notecompose.presentation.util.Screen
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    navController: NavController,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val bookmarkedNotes = state.notes.filter { it.isBookmarked }
    val context = LocalContext.current

    var noteToUnlock by remember { mutableStateOf<Note?>(null) }
    var pinEntryValue by remember { mutableStateOf("") }
    var showUnlockDialog by remember { mutableStateOf(false) }

    if (showUnlockDialog && noteToUnlock != null) {
        AlertDialog(
            onDismissRequest = { 
                showUnlockDialog = false
                pinEntryValue = ""
            },
            title = { Text("Enter PIN to unlock") },
            text = {
                OutlinedTextField(
                    value = pinEntryValue,
                    onValueChange = { if (it.length <= 4) pinEntryValue = it },
                    label = { Text("4-digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pinEntryValue == noteToUnlock?.pin) {
                        navController.navigate(Screen.AddEditNoteScreen.route + "?noteId=${noteToUnlock?.id}")
                        showUnlockDialog = false
                        pinEntryValue = ""
                    } else {
                        Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showUnlockDialog = false
                    pinEntryValue = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bookmarked Notes", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (bookmarkedNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No bookmarked notes yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(bookmarkedNotes) { note ->
                    NoteItem(
                        note = note,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (note.pin != null) {
                                    noteToUnlock = note
                                    showUnlockDialog = true
                                } else {
                                    navController.navigate(
                                        Screen.AddEditNoteScreen.route +
                                                "?noteId=${note.id}"
                                    )
                                }
                            },
                        onDeleteClick = {
                            viewModel.onEvent(NotesEvent.DeleteNote(note))
                        },
                        onEditClick = {
                            if (note.pin != null) {
                                noteToUnlock = note
                                showUnlockDialog = true
                            } else {
                                navController.navigate(
                                    Screen.AddEditNoteScreen.route +
                                            "?noteId=${note.id}"
                                )
                            }
                        },
                        onBookmarkClick = {
                            viewModel.onEvent(NotesEvent.ToggleBookmark(note))
                        },
                        onLockClick = {
                             if (note.pin != null) {
                                noteToUnlock = note
                                showUnlockDialog = true
                            } else {
                                navController.navigate(
                                    Screen.AddEditNoteScreen.route +
                                            "?noteId=${note.id}"
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
