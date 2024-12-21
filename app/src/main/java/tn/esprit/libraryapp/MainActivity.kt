package tn.esprit.libraryapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import tn.esprit.libraryapp.screens.EPubReaderScreen
import tn.esprit.libraryapp.screens.SpeechScreen
import tn.esprit.libraryapp.services.TokenManagerProvider
import tn.esprit.libraryapp.ui.theme.LibraryAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                0
            )
        }
        setContent {
            LibraryAppTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                TokenManagerProvider.initialize(this)

                val authRoutes = listOf(
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
                    AppNavHost(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        startDestination = NavigationItem.Login.route
                    )
                }
            }
//            LibraryAppTheme {
//                EPubReaderScreen(epubUrl = "https://www.gutenberg.org/ebooks/1777.epub3.images")
//            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: androidx.navigation.NavHostController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Bookmarks,
        NavigationItem.Speech
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
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

