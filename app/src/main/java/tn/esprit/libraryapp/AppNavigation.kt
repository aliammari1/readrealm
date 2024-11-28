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
        }
    }
}

enum class Screen {
    REGISTER,
    LOGIN,
    FORGOT_PASSWORD,
    OTP,
    HOME,
    RESET_PASSWORD
}

sealed class NavigationItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : NavigationItem("home", Icons.Filled.Home, "Home")
    object Login : NavigationItem("login", Icons.Filled.MailOutline, "Login")
    object Register : NavigationItem("register", Icons.Filled.Add, "Register")
    object ForgotPassword : NavigationItem("forgot_password", Icons.Filled.Lock, "Forgot Password")
    object OTP : NavigationItem("otp", Icons.Filled.Check, "OTP")
    object ResetPassword : NavigationItem("reset_password", Icons.Filled.Refresh, "Reset Password")
}