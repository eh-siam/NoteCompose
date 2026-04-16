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
import com.example.notecompose.presentation.auth.login.LoginScreen
import com.example.notecompose.presentation.auth.signup.SignUpScreen
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
        
        // Aesthetic exit animation for the system splash
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

data class BannerItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradient: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email ?: "User"
    val userName = userEmail.substringBefore("@")

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        auth.signOut()
                        navController.navigate(Screen.LoginScreen.route) {
                            popUpTo(0) { inclusive = true }
                        }
                        showLogoutDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    val bannerItems = listOf(
        BannerItem(
            title = "Organize Your Thoughts",
            subtitle = "Keep your ideas structured and never miss a detail.",
            icon = Icons.AutoMirrored.Filled.Notes,
            gradient = listOf(Color(0xFFBBDEFB), Color(0xFF64B5F6))
        ),
        BannerItem(
            title = "Secure Your Privacy",
            subtitle = "Keep sensitive notes safe with 4-digit PIN lock.",
            icon = Icons.Default.Lock,
            gradient = listOf(Color(0xFFFFE0B2), Color(0xFFFFB74D))
        ),
        BannerItem(
            title = "Quick Bookmarks",
            subtitle = "Save important notes for faster access anytime.",
            icon = Icons.Default.Star,
            gradient = listOf(Color(0xFFC8E6C9), Color(0xFF81C784))
        )
    )
    val pagerState = rememberPagerState(pageCount = { bannerItems.size })

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = userName.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }

            // Enhanced Banner with Gradients and Icons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) { page ->
                    val item = bannerItems[page]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(item.gradient))
                    ) {
                        // Decorative large background icon
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.CenterEnd)
                                .offset(x = 20.dp)
                                .alpha(0.15f),
                            tint = Color.White
                        )
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray.copy(alpha = 0.8f),
                                modifier = Modifier.fillMaxWidth(0.7f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Dot Indicator
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
            }

            // Dashboard Grid
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    HomeCard(
                        title = "All Notes",
                        subtitle = "Manage your ideas",
                        icon = Icons.Filled.Folder,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        iconColor = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate(Screen.NotesScreen.route) }
                    )
                }
                item {
                    HomeCard(
                        title = "Bookmarks",
                        subtitle = "Saved for later",
                        icon = Icons.Default.Bookmark,
                        color = Color(0xFFFFE0B2),
                        iconColor = Color(0xFFF57C00),
                        onClick = { navController.navigate(Screen.BookmarksScreen.route) }
                    )
                }
                item {
                    HomeCard(
                        title = "Complete",
                        subtitle = "Finished tasks",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFFC8E6C9), // Light Green
                        iconColor = Color(0xFF388E3C),
                        onClick = { /* Complete */ }
                    )
                }
                item {
                    HomeCard(
                        title = "Drafts",
                        subtitle = "Unfinished work",
                        icon = Icons.Filled.EditNote,
                        color = Color(0xFFBBDEFB),
                        iconColor = Color(0xFF1976D2),
                        onClick = { /* Drafts */ }
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HomeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    iconColor: Color = Color.DarkGray,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Enhanced Decorative design
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Top-right large circle
                drawArc(
                    color = Color.White.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = true,
                    topLeft = Offset(size.width * 0.65f, -size.height * 0.15f),
                    size = Size(size.width * 0.7f, size.width * 0.7f)
                )
                // Bottom-left smaller circle for more depth
                drawArc(
                    color = Color.Black.copy(alpha = 0.03f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = true,
                    topLeft = Offset(-size.width * 0.15f, size.height * 0.7f),
                    size = Size(size.width * 0.4f, size.width * 0.4f)
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.5f), shape = RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(24.dp),
                            tint = iconColor
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp).alpha(0.4f),
                        tint = Color.DarkGray
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.2.sp
                        ),
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
