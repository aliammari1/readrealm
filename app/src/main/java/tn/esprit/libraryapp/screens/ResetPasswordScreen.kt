package tn.esprit.libraryapp.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.esprit.libraryapp.NavigationItem
import tn.esprit.libraryapp.R
import tn.esprit.libraryapp.components.MyTextField
import tn.esprit.libraryapp.models.ForgotPasswordRequest
import tn.esprit.libraryapp.viewModel.AuthViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    email: String
) {
    val viewModel: AuthViewModel = viewModel()
    var passwordState by remember { mutableStateOf("") }
    var confirmPasswordState by remember { mutableStateOf("") }
    val forgotPasswordResult by viewModel.forgotPasswordResult.observeAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // Header section with an image and text
        Column {
            Image(
                painter = painterResource(R.drawable.login),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Adjust the height if necessary
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Set Your Password ",
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp
            )
            Text(
                text = "In order to keep your account safe you need to create a strong password.",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
        Column {
            MyTextField(
                textFieldState = passwordState,
                onTextChange = { passwordState = it },
                hint = "Password",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(60.dp))

            MyTextField(
                textFieldState = confirmPasswordState,
                onTextChange = { confirmPasswordState = it },
                hint = "Confirm Password",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Login Button
        Button(
            onClick = {
                viewModel.forgotPassword(
                    ForgotPasswordRequest(
                        email = email,
                        password = passwordState,
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Login",
                fontSize = 17.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        forgotPasswordResult?.let {
            if (it.isSuccess) {
                Toast.makeText(
                    LocalContext.current,
                    "Password forgotten successful",
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate(NavigationItem.Login.route)
            } else {
                Toast.makeText(LocalContext.current, "Password reset failed", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}