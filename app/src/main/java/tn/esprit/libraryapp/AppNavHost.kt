package tn.esprit.libraryapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import tn.esprit.libraryapp.screens.*

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = NavigationItem.Home.route
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
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
                navController = navController,
                email = email ?: ""
            )
        }
        composable(NavigationItem.ResetPassword.route + "/{email}") {
            val email = it.arguments?.getString("email")
            ResetPasswordScreen(navController = navController, email = email ?: "")
        }
    }
}