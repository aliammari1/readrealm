package tn.esprit.libraryapp.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import tn.esprit.libraryapp.models.VerifyEmailRequest
import tn.esprit.libraryapp.viewModel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    navController: NavHostController,
    email: String
) {
    val viewModel: AuthViewModel = viewModel()
    val verificationCode = remember { mutableStateListOf<String>().apply { repeat(6) { add("") } } }
    val verifyEmailResult by viewModel.verifyEmailResult.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xffffffff))
            .padding(20.dp)
    ) {

        Text(
            modifier = Modifier
                .width(340.dp),
            text = "Verification Code ",
            color = Color(0xff000000),
            fontSize = 30.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Left,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            modifier = Modifier
                .padding(top = 20.dp)
                .width(347.dp),
            text = "Please enter the  verification code sent to your email.",
            color = Color(0xb2000000),
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Left,
            overflow = TextOverflow.Ellipsis
        )

        // Verification Code Input Fields (One for each digit)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(start = 50.dp, top = 20.dp) // This adds space before the section
        ) {
            Text(
                text = "Verification Code",
                color = Color(0xff000000),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Left
            )

            // Added more space here
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until 6) {
                    OutlinedTextField(
                        value = verificationCode[i],
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                verificationCode[i] = newValue
                            }
                        },
                        maxLines = 1,
                        modifier = Modifier
                            .width(50.dp)
                            .border(3.dp, Color(0x7f000000), RoundedCornerShape(15.dp)),
                        singleLine = true,
                        placeholder = { Text("", color = Color(0x7f000000)) }
                    )
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                val code = verificationCode.joinToString("")
                viewModel.verifyEmail(VerifyEmailRequest(email, code))
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "Submit",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }

    verifyEmailResult?.let {
        if (it.isSuccess) {
            Toast.makeText(LocalContext.current, "Verification successful", Toast.LENGTH_SHORT)
                .show()
            isSheetOpen = true
        } else {
            Toast.makeText(LocalContext.current, "Verification failed", Toast.LENGTH_SHORT).show()
        }
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen = false }
        ) {
            ResetPasswordScreen(
                navController = navController,
                email = email
            )
        }
    }

}

