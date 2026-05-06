package com.example.notecompose.presentation.util

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.notecompose.presentation.add_edit_note.AddEditNoteScreen
import com.example.notecompose.presentation.auth.login.LoginScreen
import com.example.notecompose.presentation.auth.signup.SignUpScreen
import com.example.notecompose.presentation.notes.NotesScreen
import com.example.notecompose.presentation.splash.SplashScreen
import com.example.notecompose.presentation.home.HomeScreen
import com.example.notecompose.presentation.bookmarks.BookmarksScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(navController: NavHostController) {
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Screen.HomeScreen.route
    } else {
        Screen.LoginScreen.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
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
