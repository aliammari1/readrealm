package tn.esprit.libraryapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            "book_details/{bookId}/{bookJson}?isBookmarked={isBookmarked}",
            arguments = listOf(
                navArgument("bookId") { type = NavType.IntType },
                navArgument("bookJson") { type = NavType.StringType },
                navArgument("isBookmarked") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            BookDetailsScreen(
                navController = navController,
                bookId = backStackEntry.arguments?.getInt("bookId") ?: 0,
                bookJson = backStackEntry.arguments?.getString("bookJson") ?: "",
                initialIsBookmarked = backStackEntry.arguments?.getBoolean("isBookmarked") == true
            )
        }
        composable(route = "ebook/{bookId}", arguments = listOf(navArgument("bookId") {
            type = NavType.IntType  // Change to StringType to match book_details
        })) {
            val bookId = it.arguments?.getInt("bookId") ?: 0
            EbookScreen(
                navController = navController, bookId = bookId
            )
        }
        composable(NavigationItem.Speech.route) {
            SpeechScreen()
        }
        composable(NavigationItem.Bookmarks.route) {
            BookmarksScreen()
        }
//        composable(NavigationItem.ReadBook.route + "/{bookId}") {
//            val bookId = it.arguments?.getInt("bookId") ?: 0
//            ReadBookScreen(book = Book(
//            ))
//        }
        composable(NavigationItem.Profile.route) {
            ProfileScreen(navController = navController)
        }
    }
}
