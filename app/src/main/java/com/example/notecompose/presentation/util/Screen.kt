package com.example.notecompose.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String? = null, val icon: ImageVector? = null) {
    object SplashScreen: Screen("splash_screen")
    object HomeScreen: Screen("home_screen", "Home", Icons.Default.Home)
    object NotesScreen: Screen("notes_screen", "Notes", Icons.AutoMirrored.Filled.Notes)
    object BookmarksScreen: Screen("bookmarks_screen", "Bookmarks", Icons.Default.Bookmark)

    object AddEditNoteScreen: Screen("add_edit_note_screen")
    object LoginScreen: Screen("login_screen")
    object SignUpScreen: Screen("signup_screen")
}
