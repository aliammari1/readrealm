package tn.esprit.libraryapp.screens

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.esprit.libraryapp.NavigationItem
import tn.esprit.libraryapp.components.MyTextField
import tn.esprit.libraryapp.models.ForgotPasswordRequest
import tn.esprit.libraryapp.viewModel.AuthViewModel

// Immersive Library Theme Colors
private val DeepLibraryBrown = Color(0xFF1A0F0A)
private val RichMahogany = Color(0xFF4A2C2A)
private val WarmLeather = Color(0xFF8B5A2B)
private val GildedGold = Color(0xFFD4AF37)
private val AncientParchment = Color(0xFFF5E6C8)
private val CandlelightGlow = Color(0xFFFFE4B5)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    email: String,
) {
    val viewModel: AuthViewModel = viewModel()
    var passwordState by remember { mutableStateOf("") }
    var confirmPasswordState by remember { mutableStateOf("") }
    val forgotPasswordResult by viewModel.forgotPasswordResult.collectAsState()
    val context = LocalContext.current

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "reset_animations")

    val keyGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "key_glow",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AncientParchment),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        spotColor = GildedGold.copy(alpha = keyGlow * 0.5f),
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(RichMahogany, WarmLeather),
                        ),
                        shape = CircleShape,
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(GildedGold, GildedGold.copy(alpha = 0.5f)),
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Key,
                    contentDescription = null,
                    tint = CandlelightGlow,
                    modifier = Modifier.size(40.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = "Forge a New Key",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    shadow = Shadow(
                        color = WarmLeather.copy(alpha = 0.5f),
                        offset = Offset(0f, 2f),
                        blurRadius = 6f,
                    ),
                ),
                color = DeepLibraryBrown,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create a powerful incantation to protect your tome from unwanted visitors",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                ),
                color = RichMahogany.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Decorative line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(2.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GildedGold.copy(alpha = 0.6f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Password Field
            Column {
                Text(
                    "New Secret Incantation",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = RichMahogany,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
                )
                MyTextField(
                    textFieldState = passwordState,
                    onTextChange = { passwordState = it },
                    hint = "Enter your new incantation",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Password Field
            Column {
                Text(
                    "Confirm Your Incantation",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = RichMahogany,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
                )
                MyTextField(
                    textFieldState = confirmPasswordState,
                    onTextChange = { confirmPasswordState = it },
                    hint = "Repeat the incantation",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Reset Button
            Button(
                onClick = {
                    if (passwordState == confirmPasswordState) {
                        viewModel.forgotPassword(
                            ForgotPasswordRequest(
                                email = email,
                                password = passwordState,
                            ),
                        )
                    } else {
                        Toast.makeText(context, "The incantations don't match!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = RichMahogany),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    RichMahogany,
                                    WarmLeather,
                                    RichMahogany,
                                ),
                            ),
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    GildedGold.copy(alpha = 0.5f),
                                    GildedGold,
                                    GildedGold.copy(alpha = 0.5f),
                                ),
                            ),
                            shape = RoundedCornerShape(16.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LockReset,
                            contentDescription = null,
                            tint = CandlelightGlow,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Seal the New Key",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            ),
                            color = CandlelightGlow,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    forgotPasswordResult?.let {
        if (it.isSuccess) {
            Toast.makeText(context, "Your new key has been forged!", Toast.LENGTH_SHORT).show()
            navController.navigate(NavigationItem.Login.route)
        } else {
            Toast.makeText(context, "The forging has failed...", Toast.LENGTH_SHORT).show()
        }
    }
}
