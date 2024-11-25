package tn.esprit.libraryapp.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import tn.esprit.libraryapp.NavigationItem
import tn.esprit.libraryapp.R
import tn.esprit.libraryapp.components.AuthOption
import tn.esprit.libraryapp.components.MyTextField
import tn.esprit.libraryapp.models.LoginRequest
import tn.esprit.libraryapp.viewModel.AuthViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: AuthViewModel = AuthViewModel()
) {
    var emailState by remember { mutableStateOf("") }
    var passwordState by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val loginResult by viewModel.loginResult.observeAsState()

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

            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "Login",
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp
            )
        }

        // Email and Password fields
        MyTextField(
            textFieldState = emailState,
            onTextChange = { emailState = it },
            hint = "Email",
            leadingIcon = Icons.Outlined.Email,
            trailingIcon = Icons.Outlined.Check,
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )
        MyTextField(
            textFieldState = passwordState,
            onTextChange = { passwordState = it },
            hint = "Password",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Remember Me and Forgot Password Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it }
                )
                Text(
                    text = "Remember",
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { isChecked = !isChecked }
                )
            }

            Text(
                text = "Forgot password",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { isSheetOpen = true }
            )
        }

        // Login Button
        Button(
            onClick = {
                viewModel.login(
                    LoginRequest(
                        emailState,
                        passwordState
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

        loginResult?.let {
            if (it.isSuccess) {
                Toast.makeText(LocalContext.current, "Login successful", Toast.LENGTH_SHORT).show()
                navController.navigate(NavigationItem.Home.route)
            } else {
                Toast.makeText(LocalContext.current, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Separator Text
        Text(
            text = "or, login with ... ",
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .alpha(0.5f)
                .padding(bottom = 1.dp) // Adjust padding below the text
        )

        // Social Media Login Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AuthOption(image = R.drawable.google)
            AuthOption(image = R.drawable.facebook)
            AuthOption(image = R.drawable.instagram)
        }

        // Footer with Sign-Up Option
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account?  ",
                fontSize = 16.sp,
            )
            Text(
                text = "Register",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { navController.navigate(NavigationItem.Register.route) }
            )
        }

        if (isSheetOpen) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { isSheetOpen = false }
            ) {
                ForgotScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}