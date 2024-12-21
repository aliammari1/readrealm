package tn.esprit.libraryapp.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.esprit.libraryapp.NavigationItem
import tn.esprit.libraryapp.R
import tn.esprit.libraryapp.components.AuthOption
import tn.esprit.libraryapp.components.MyTextField
import tn.esprit.libraryapp.models.RegisterRequest
import tn.esprit.libraryapp.viewModel.AuthViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val viewModel: AuthViewModel = viewModel()
    var nameState by remember { mutableStateOf("") }
    var emailState by remember { mutableStateOf("") }
    var passwordState by remember { mutableStateOf("") }
    val registerResult by viewModel.registerResult.observeAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Column {
            Image(
                painter = painterResource(R.drawable.login),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Register",
                fontWeight = FontWeight.SemiBold,
                fontSize = 50.sp
            )
        }
        Text(
            text = "or, login with ... ",
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .alpha(0.5f)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AuthOption(image = R.drawable.google)
            AuthOption(image = R.drawable.facebook)
            AuthOption(image = R.drawable.instagram)
        }

        MyTextField(
            textFieldState = nameState,
            onTextChange = { nameState = it },
            hint = "Name",
            leadingIcon = Icons.Outlined.AccountCircle,
            modifier = Modifier.fillMaxWidth()
        )
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

        Button(
            onClick = {
                viewModel.register(
                    RegisterRequest(
                        username = nameState,
                        email = emailState,
                        password = passwordState
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Register",
                fontSize = 17.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        registerResult?.let {
            if (it.isSuccess) {
                Toast.makeText(LocalContext.current, "Registration successful", Toast.LENGTH_SHORT)
                    .show()
                navController.navigate(NavigationItem.Login.route)
            } else {
                Toast.makeText(LocalContext.current, "Registration failed", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Already have an account?  ",
                fontSize = 16.sp,
            )
            Text(
                text = "Login",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { navController.navigate(NavigationItem.Login.route) }
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
    }
}