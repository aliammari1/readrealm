package tn.esprit.libraryapp.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.esprit.libraryapp.NavigationItem
import tn.esprit.libraryapp.models.GenerateEmailRequest
import tn.esprit.libraryapp.viewModel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val viewModel: AuthViewModel = viewModel()
    val email = remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()
    var isSheetopen by rememberSaveable {
        mutableStateOf(false)
    }
    val generateEmailResult by viewModel.generateEmailResult.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xffffffff))
            .padding(20.dp)
    ) {
        // Text - Forgot password
        Text(
            modifier = Modifier
                .width(200.dp),
            text = "Forgot password?",
            color = Color(0xff000000),
            fontSize = 30.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Left,
            overflow = TextOverflow.Ellipsis
        )

        // Description Text
        Text(
            modifier = Modifier
                .padding(top = 20.dp)
                .width(347.dp),
            text = "Don’t worry! It happens. Please enter the email associated with your account.",
            color = Color(0xb2000000),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Left,
            overflow = TextOverflow.Ellipsis
        )

        // Email Input Field
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .padding(top = 20.dp)
        ) {
            Text(
                text = "Email address",
                color = Color(0xff000000),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Left
            )
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                placeholder = { Text("Enter your email address", color = Color(0x7f000000)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xffd8dadc), RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                singleLine = true
            )
        }

        // Send Code Button
        Button(
            onClick = {
                viewModel.generateEmail(GenerateEmailRequest(email.value))
                // viewModel.forgotPassword(ForgotPasswordRequest(email.value))
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text(
                text = "Send code",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

        generateEmailResult?.let {
            if (it.isSuccess) {
                Toast.makeText(LocalContext.current, "Code sent to email", Toast.LENGTH_SHORT)
                    .show()
                isSheetopen = true
            } else {
                Toast.makeText(LocalContext.current, "Failed to send code", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Log in Link
        TextButton(
            onClick = { navController.navigate(NavigationItem.Login.route) },
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 90.dp, top = 10.dp)
        ) {
            Text(
                text = "Remember password? Log in",
                color = Color(0xff000000),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Left,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isSheetopen) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { isSheetopen = false }
            ) {
                OtpScreen(
                    navController = navController,
                    email = email.value
                )
            }
        }
    }
}



