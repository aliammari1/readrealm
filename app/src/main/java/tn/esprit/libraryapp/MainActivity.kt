package tn.esprit.libraryapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import tn.esprit.libraryapp.services.TokenManagerProvider
import tn.esprit.libraryapp.ui.theme.LibraryAppTheme
import tn.esprit.libraryapp.viewModel.BookViewModel

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }
        setContent {
            LibraryAppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                TokenManagerProvider.initialize(this)

                val authRoutes =
                    listOf(
                        NavigationItem.Login.route,
                        NavigationItem.Register.route,
                        NavigationItem.ForgotPassword.route,
                        NavigationItem.OTP.route,
                        NavigationItem.ResetPassword.route
                    )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute !in authRoutes) {
                            BottomNavigationBar(navController)
                        }
                    }
                ) { innerPadding ->
                    MainContent(navController, NavigationItem.Login.route, innerPadding)
                }
            }
//            LibraryAppTheme {
//                EPubReaderScreen(bookUrl = "https://www.gutenberg.org/ebooks/1777.epub3.images")
//            }
        }
    }

    @Composable
    fun MainContent(
        navController: NavHostController,
        startDestination: String,
        innerPadding: PaddingValues
    ) {
        val viewModel: BookViewModel = viewModel()

        // Remove LaunchedEffect and librarian initialization
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavHost(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                startDestination = startDestination
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items =
        listOf(
            NavigationItem.Home,
            NavigationItem.Bookmarks,
            NavigationItem.BookChannel,
            NavigationItem.Speech,
            NavigationItem.Profile
        )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .height(72.dp),
        shape = RoundedCornerShape(36.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                    Brush.linearGradient(
                        colors =
                        listOf(
                            MaterialTheme.colorScheme
                                .surface,
                            MaterialTheme.colorScheme
                                .surface.copy(
                                    alpha = 0.9f
                                )
                        )
                    )
                )
        ) {
            // Animated background pattern
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pattern =
                    Path().apply {
                        moveTo(0f, size.height * 0.4f)
                        cubicTo(
                            size.width * 0.2f,
                            size.height * 0.2f,
                            size.width * 0.8f,
                            size.height * 0.6f,
                            size.width,
                            size.height * 0.4f
                        )
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                drawPath(path = pattern, color = Color.White.copy(alpha = 0.2f))
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val selected = currentRoute == item.route
                    FloatingNavItem(
                        item = item,
                        selected = selected,
                        index = index,
                        onItemClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    item: NavigationItem,
    selected: Boolean,
    index: Int,
    onItemClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale = remember { Animatable(1f) }

    // Pulse animation for selected item
    val pulseScale by
    infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec =
        infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    // Rotation animation for icon
    val rotation by
    animateFloatAsState(
        targetValue = if (selected) 360f else 0f,
        animationSpec = spring(dampingRatio = 0.3f, stiffness = 300f),
        label = ""
    )

    LaunchedEffect(selected) {
        if (selected) {
            scale.animateTo(
                targetValue = 1.2f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f)
            )
        } else {
            scale.animateTo(1f)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onItemClick)
            .padding(8.dp)
    ) {
        Box(
            modifier =
            Modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = if (selected) pulseScale else 1f
                    scaleY = if (selected) pulseScale else 1f
                    rotationY = rotation
                }
                .background(
                    brush =
                    if (selected) {
                        Brush.radialGradient(
                            colors =
                            listOf(
                                MaterialTheme
                                    .colorScheme
                                    .primary,
                                MaterialTheme
                                    .colorScheme
                                    .primary.copy(
                                        alpha = 0.8f
                                    )
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors =
                            listOf(
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant,
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant
                                    .copy(
                                        alpha =
                                        0.8f
                                    )
                            )
                        )
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(24.dp),
                tint =
                if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        AnimatedVisibility(
            visible = selected,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
