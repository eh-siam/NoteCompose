package com.example.notecompose.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.notecompose.presentation.util.BottomNavigationBar
import com.example.notecompose.presentation.util.Screen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

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

    // Animation state for the cards
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300) // Longer delay to ensure layout is ready and animation is visible
        visible = true
    }

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

            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    AnimatedHomeCard(
                        visible = visible,
                        delay = 0,
                        title = "All Notes",
                        subtitle = "Manage your ideas",
                        icon = Icons.Filled.Folder,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        iconColor = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate(Screen.AllNotesScreen.route) }
                    )
                }
                item {
                    AnimatedHomeCard(
                        visible = visible,
                        delay = 150,
                        title = "Bookmarks",
                        subtitle = "Saved for later",
                        icon = Icons.Default.Bookmark,
                        color = Color(0xFFFFE0B2),
                        iconColor = Color(0xFFF57C00),
                        onClick = { navController.navigate(Screen.BookmarksScreen.route) }
                    )
                }
                item {
                    AnimatedHomeCard(
                        visible = visible,
                        delay = 300,
                        title = "Complete",
                        subtitle = "Finished tasks",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFFC8E6C9),
                        iconColor = Color(0xFF388E3C),
                        onClick = { navController.navigate(Screen.CompleteNotesScreen.route) }
                    )
                }
                item {
                    AnimatedHomeCard(
                        visible = visible,
                        delay = 450,
                        title = "Drafts",
                        subtitle = "Unfinished work",
                        icon = Icons.Filled.EditNote,
                        color = Color(0xFFBBDEFB),
                        iconColor = Color(0xFF1976D2),
                        onClick = { /* Drafts navigation logic here if needed */ }
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedHomeCard(
    visible: Boolean,
    delay: Int,
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    val entranceAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = delay, easing = FastOutSlowInEasing),
        label = "entranceAlpha"
    )
    val entranceScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = tween(durationMillis = 800, delayMillis = delay, easing = FastOutSlowInEasing),
        label = "entranceScale"
    )
    val entranceOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 50f,
        animationSpec = tween(durationMillis = 800, delayMillis = delay, easing = FastOutSlowInEasing),
        label = "entranceOffset"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                alpha = entranceAlpha
                scaleX = entranceScale
                scaleY = entranceScale
                translationY = entranceOffset
            }
    ) {
        HomeCard(
            title = title,
            subtitle = subtitle,
            icon = icon,
            color = color,
            iconColor = iconColor,
            onClick = onClick
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 1.dp else 4.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.White.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = true,
                    topLeft = Offset(size.width * 0.65f, -size.height * 0.15f),
                    size = Size(size.width * 0.7f, size.width * 0.7f)
                )
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
