package tn.esprit.libraryapp

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

sealed class NavigationItem(val route: String) {
    object Register : NavigationItem(Screen.REGISTER.name)
    object Login : NavigationItem(Screen.LOGIN.name)
    object ForgotPassword : NavigationItem(Screen.FORGOT_PASSWORD.name)
    object OTP : NavigationItem(Screen.OTP.name)
    object Home : NavigationItem(Screen.HOME.name)
    object ResetPassword : NavigationItem(Screen.RESET_PASSWORD.name)
}
