package com.example.notecompose.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.notecompose.R
import com.example.notecompose.presentation.util.Screen
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random

@Composable
fun SplashScreen(navController: NavController) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash_anim))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    // Text animations
    val textAlpha by animateFloatAsState(
        targetValue = if (progress > 0.4f) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "textAlpha"
    )

    val textScale by animateFloatAsState(
        targetValue = if (progress > 0.4f) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "textScale"
    )

    // Icon animation (scale and fade)
    val iconAlpha by animateFloatAsState(
        targetValue = if (progress > 0.2f) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "iconAlpha"
    )

    // Background particle animation
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    // Twinkle animation for stars
    val twinkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    LaunchedEffect(key1 = progress) {
        if (progress == 1f) {
            val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
                Screen.HomeScreen.route
            } else {
                Screen.LoginScreen.route
            }
            navController.navigate(startDestination) {
                popUpTo(Screen.SplashScreen.route) { inclusive = true } }
        }
    }

    // Aesthetic color palette
    val gradientColors = listOf(
        Color(0xFF0D0D1A), // Even deeper Midnight
        Color(0xFF16213E), // Dark Navy
        Color(0xFF0F3460)  // Deep Blue
    )

    // Generate random star positions once
    val stars = remember {
        List(35) {
            Offset(Random.nextFloat(), Random.nextFloat())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background particles/glow/stars
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF4ECCA3).copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.2f + particleOffset)
                ),
                radius = 600f,
                center = Offset(size.width * 0.2f, size.height * 0.2f + particleOffset)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE94560).copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.7f - particleOffset)
                ),
                radius = 700f,
                center = Offset(size.width * 0.8f, size.height * 0.7f - particleOffset)
            )

            // Twinkling Stars
            stars.forEach { star ->
                drawCircle(
                    color = Color.White.copy(alpha = twinkleAlpha * Random.nextFloat()),
                    radius = 2f,
                    center = Offset(star.x * size.width, star.y * size.height)
                )
            }
        }

        // Top Content: App Icon and Decorative Label
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon with aesthetic border and shadow
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .alpha(iconAlpha)
                    .scale(textScale)
                    .shadow(20.dp, CircleShape, spotColor = Color(0xFF4ECCA3))
                    .border(2.dp, Brush.linearGradient(listOf(Color(0xFF4ECCA3), Color.Transparent)), CircleShape)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Icon",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "YOUR CREATIVE SPACE",
                modifier = Modifier.alpha(textAlpha * 0.6f),
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .size(240.dp)
                    .graphicsLayer {
                        val scale = 1f + (0.05f * progress)
                        scaleX = scale
                        scaleY = scale
                    }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textAlpha)
                    .scale(textScale)
            ) {
                Text(
                    text = "Welcome to",
                    style = TextStyle(
                        color = Color(0xFF4ECCA3).copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 4.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "NoteCompose",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        shadow = Shadow(
                            color = Color(0xFF4ECCA3).copy(alpha = 0.5f),
                            offset = Offset(0f, 0f),
                            blurRadius = 15f
                        )
                    )
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "CRAFT YOUR THOUGHTS",
                    style = TextStyle(
                        color = Color(0xFF4ECCA3),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 6.sp
                    )
                )
            }
        }
        
        // Aesthetic footer
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .alpha(textAlpha * 0.7f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(Color(0xFF4ECCA3).copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "EST. 2024",
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 3.sp
                    )
                )
            }
        }
    }
}
