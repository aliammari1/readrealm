package tn.esprit.libraryapp

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import tn.esprit.libraryapp.screens.*

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = NavigationItem.Home.route
) {
    NavHost(
        modifier = modifier, navController = navController, startDestination = startDestination
    ) {
        composable(NavigationItem.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(NavigationItem.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(NavigationItem.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(NavigationItem.ForgotPassword.route) {
            ForgotScreen(navController = navController)
        }
        composable(NavigationItem.OTP.route + "/{email}") {
            val email = it.arguments?.getString("email")
            OtpScreen(
                navController = navController, email = email ?: ""
            )
        }
        composable(NavigationItem.ResetPassword.route + "/{email}") {
            val email = it.arguments?.getString("email")
            ResetPasswordScreen(navController = navController, email = email ?: "")
        }
        composable(
            "book_details/{bookId}",
            arguments = listOf(
                navArgument("bookId") { type = NavType.IntType },
            )
        ) { backStackEntry ->
            BookDetailsScreen(
                navController = navController,
                bookId = backStackEntry.arguments?.getInt("bookId") ?: 0,
            )
        }
        composable(NavigationItem.Speech.route) {
            SpeechScreen()
        }
        composable(NavigationItem.Bookmarks.route) {
            MyLibraryScreen()
        }
        composable(
            route = NavigationItem.ReadBook.route + "/{encodedUrl}",
            arguments = listOf(
                navArgument("encodedUrl") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("encodedUrl")
            val decodedUrl = encodedUrl?.let { Uri.decode(it) }
            EPubReaderScreen(bookUrl = decodedUrl ?: "")
        }
        composable(NavigationItem.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(NavigationItem.BookChannel.route) {
            BookChatScreen()
        }
        composable(
            route = "book_chat/{bookId}",
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookChatScreen(
                bookId = bookId,
                viewModel = viewModel()
            )
        }
    }
}
