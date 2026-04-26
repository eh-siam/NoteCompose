package com.example.notecompose.presentation.notes

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.notecompose.domain.model.Note
import com.example.notecompose.presentation.notes.components.NoteItem
import com.example.notecompose.presentation.notes.components.OrderSection
import com.example.notecompose.presentation.util.BottomNavigationBar
import com.example.notecompose.presentation.util.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    navController: NavController,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val categoryListState = rememberLazyListState()
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddEditNoteScreen.route + "?noteId=-1")
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add note")
            }
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent notes",
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(
                    onClick = {
                        viewModel.onEvent(NotesEvent.ToggleOrderSection)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort"
                    )
                }
            }
            AnimatedVisibility(
                visible = state.isOrderSectionVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                OrderSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    noteOrder = state.noteOrder,
                    onOrderChange = {
                        viewModel.onEvent(NotesEvent.Order(it))
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal Category
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LazyRow(
                    state = categoryListState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(Note.categories) { category ->
                        val isSelected = state.selectedCategory == category
                        val categoryColor = when (category) {
                            "Work" -> Color(0xFF90CAF9)
                            "Birthday" -> Color(0xFFF48FB1)
                            "Occasions" -> Color(0xFFA5D6A7)
                            "Important" -> Color(0xFFFFAB91)
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.onEvent(NotesEvent.ChangeCategory(category))
                                },
                            color = if (isSelected) categoryColor else categoryColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp),
                            border = if (!isSelected) BorderStroke(1.dp, categoryColor.copy(alpha = 0.5f)) else null
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else categoryColor
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.height(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(Note.categories.size) { index ->
                        val category = Note.categories[index]
                        val categoryColor = when (category) {
                            "Work" -> Color(0xFF90CAF9)
                            "Birthday" -> Color(0xFFF48FB1)
                            "Important" -> Color(0xFFFFAB91)
                            "Occasions" -> Color(0xFFA5D6A7)
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(if (state.selectedCategory == category) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (state.selectedCategory == category) categoryColor else categoryColor.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.notes) { note ->
                    NoteItem(
                        note = note,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (note.pin != null) {
                                    noteToUnlock = note
                                    showUnlockDialog = true
                                } else {
                                    navController.navigate(Screen.AddEditNoteScreen.route + "?noteId=${note.id}")
                                }
                            },
                        onDeleteClick = {
                            viewModel.onEvent(NotesEvent.DeleteNote(note))
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Note deleted",
                                    actionLabel = "Undo"
                                )
                                if(result == SnackbarResult.ActionPerformed) {
                                    viewModel.onEvent(NotesEvent.RestoreNote)
                                }
                            }
                        },
                        onEditClick = {
                            if (note.pin != null) {
                                noteToUnlock = note
                                showUnlockDialog = true
                            } else {
                                navController.navigate(Screen.AddEditNoteScreen.route + "?noteId=${note.id}")
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
                                navController.navigate(Screen.AddEditNoteScreen.route + "?noteId=${note.id}")
                            }
                        },
                        onFinishedClick = {
                            viewModel.onEvent(NotesEvent.FinishNote(note))
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
