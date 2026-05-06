package com.example.notecompose

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notecompose.domain.model.Note
import com.example.notecompose.presentation.add_edit_note.AddEditNoteScreen
import com.example.notecompose.presentation.all_notes.AllNotesScreen
import com.example.notecompose.presentation.auth.login.LoginScreen
import com.example.notecompose.presentation.auth.signup.SignUpScreen
import com.example.notecompose.presentation.complete_notes.CompleteNotesScreen
import com.example.notecompose.presentation.home.HomeScreen
import com.example.notecompose.presentation.notes.NotesEvent
import com.example.notecompose.presentation.notes.NotesScreen
import com.example.notecompose.presentation.notes.NotesViewModel
import com.example.notecompose.presentation.notes.components.NoteItem
import com.example.notecompose.presentation.splash.SplashScreen
import com.example.notecompose.presentation.util.BottomNavigationBar
import com.example.notecompose.presentation.util.Screen
import com.example.notecompose.ui.theme.NoteComposeTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.TRANSLATION_Y,
                0f,
                -splashScreenView.view.height.toFloat()
            )
            slideUp.interpolator = AnticipateInterpolator()
            slideUp.duration = 500L
            slideUp.doOnEnd { splashScreenView.remove() }
            slideUp.start()
        }

        enableEdgeToEdge()
        setContent {
            NoteComposeTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.SplashScreen.route
                    ) {
                        composable(route = Screen.SplashScreen.route) {
                            SplashScreen(navController = navController)
                        }
                        
                        composable(route = Screen.LoginScreen.route) {
                            LoginScreen(navController = navController)
                        }

                        composable(route = Screen.SignUpScreen.route) {
                            SignUpScreen(navController = navController)
                        }
                        
                        composable(route = Screen.HomeScreen.route) {
                            HomeScreen(navController = navController)
                        }
                        
                        composable(route = Screen.NotesScreen.route) {
                            NotesScreen(navController = navController)
                        }

                        composable(route = Screen.AllNotesScreen.route) {
                            AllNotesScreen(navController = navController)
                        }

                        composable(route = Screen.CompleteNotesScreen.route) {
                            CompleteNotesScreen(navController = navController)
                        }
                        
                        composable(route = Screen.BookmarksScreen.route) {
                            BookmarksScreen(navController = navController)
                        }
                        
                        composable(
                            route = Screen.AddEditNoteScreen.route +
                                    "?noteId={noteId}",
                            arguments = listOf(
                                navArgument(name = "noteId") {
                                    type = NavType.IntType
                                    defaultValue = -1
                                }
                            )
                        ) {
                            AddEditNoteScreen(
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    navController: NavController,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val bookmarkedNotes = state.notes.filter { it.isBookmarked }
    val scope = rememberCoroutineScope()
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
                        text = "No bookmarked notes yet.",
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
