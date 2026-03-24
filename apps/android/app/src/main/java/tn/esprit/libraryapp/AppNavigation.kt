package tn.esprit.libraryapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

class AppNavigation {
    fun navigateTo(screen: Screen, navController: NavHostController) {
        when (screen) {
            Screen.REGISTER -> navController.navigate(NavigationItem.Register.route)
            Screen.LOGIN -> navController.navigate(NavigationItem.Login.route)
            Screen.FORGOT_PASSWORD -> navController.navigate(NavigationItem.ForgotPassword.route)
            Screen.OTP -> navController.navigate(NavigationItem.OTP.route)
            Screen.HOME -> navController.navigate(NavigationItem.Home.route)
            Screen.RESET_PASSWORD -> navController.navigate(NavigationItem.ResetPassword.route)
            Screen.BOOK_DETAILS -> navController.navigate(NavigationItem.BookDetails.route)
            Screen.SPEECH -> navController.navigate(NavigationItem.Speech.route)
            Screen.BOOKMARKS -> navController.navigate(NavigationItem.Bookmarks.route)
            Screen.READ_BOOK -> navController.navigate(NavigationItem.ReadBook.route)
            Screen.PROFILE -> navController.navigate(NavigationItem.Profile.route)
            Screen.BOOK_CHANNEL -> navController.navigate(NavigationItem.BookChannel.route)
            Screen.BOOK_CHAT -> navController.navigate(NavigationItem.BookChat.route)
            Screen.BOOK_EXPERIENCE -> navController.navigate(NavigationItem.BookExperience.route)
        }
    }
}

enum class Screen {
    REGISTER,
    LOGIN,
    FORGOT_PASSWORD,
    OTP,
    HOME,
    RESET_PASSWORD,
    BOOK_DETAILS,
    SPEECH,
    BOOKMARKS,
    READ_BOOK,
    PROFILE,
    BOOK_CHANNEL,
    BOOK_CHAT,
    BOOK_EXPERIENCE,
}

sealed class NavigationItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : NavigationItem("home", Icons.Filled.Home, "Home")
    object Login : NavigationItem("login", Icons.Filled.MailOutline, "Login")
    object Register : NavigationItem("register", Icons.Filled.Add, "Register")
    object ForgotPassword : NavigationItem("forgot_password", Icons.Filled.Lock, "Forgot Password")
    object OTP : NavigationItem("otp", Icons.Filled.Check, "OTP")
    object ResetPassword : NavigationItem("reset_password", Icons.Filled.Refresh, "Reset Password")
    object BookDetails : NavigationItem("book_details", Icons.Filled.Info, "Book Details")
    object Speech : NavigationItem("speech", Icons.Filled.Mic, "Speech")
    object Bookmarks : NavigationItem("bookmarks", Icons.Filled.Bookmark, "Bookmarks")
    object ReadBook : NavigationItem("read_book", Icons.Filled.Book, "Read Book")
    object Profile : NavigationItem("profile", Icons.Filled.Person, "Profile")
    object BookChannel : NavigationItem("book_channel", Icons.Filled.List, "Book Channel")
    object BookChat : NavigationItem("book_chat/{bookId}", Icons.Filled.Chat, "Book Chat")
    object Explore : NavigationItem("explore", Icons.Filled.Explore, "Explore")
    object BookExperience : NavigationItem("book_experience/{bookId}/{bookTitle}", Icons.Filled.Explore, "Book Experience")
}
