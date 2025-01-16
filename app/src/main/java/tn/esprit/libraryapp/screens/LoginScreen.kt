package tn.esprit.libraryapp.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.esprit.libraryapp.NavigationItem
import tn.esprit.libraryapp.R
import tn.esprit.libraryapp.components.MyTextField
import tn.esprit.libraryapp.components.ParticleEffect
import tn.esprit.libraryapp.models.LoginRequest
import tn.esprit.libraryapp.viewModel.AuthViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val viewModel: AuthViewModel = viewModel()
    var isChecked by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val loginResult by viewModel.loginResult.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(loginResult) {
        loginResult?.let {
            if (it.isSuccess) {
                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                viewModel.clearLoginResult()
                navController.navigate(NavigationItem.Home.route) {
                    popUpTo(NavigationItem.Login.route) { inclusive = true }
                }
            } else {
                Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                viewModel.clearLoginResult()
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by
        infiniteTransition.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.03f,
            animationSpec =
            infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "",
        )

    val rotation by
        infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec =
            infiniteRepeatable(
                animation = tween(4000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "",
        )

    val scrollState = rememberScrollState()
    300.dp

    Box(modifier = modifier.fillMaxSize()) {
        // Enhanced background with dynamic particles
        ParticleEffect()

        // Animated gradient background
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                    Brush.verticalGradient(
                        colors =
                        listOf(
                            MaterialTheme.colorScheme
                                .primary.copy(
                                    alpha = 0.15f,
                                ),
                            MaterialTheme.colorScheme
                                .secondary.copy(
                                    alpha = 0.1f,
                                ),
                            MaterialTheme.colorScheme
                                .tertiary.copy(
                                    alpha = 0.05f,
                                ),
                            MaterialTheme.colorScheme
                                .surface,
                        ),
                    ),
                ),
        )

        // 3D Floating Books Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.1f),
        ) {
            repeat(12) { index ->
                val rotation by
                    rememberInfiniteTransition(label = "")
                        .animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec =
                            infiniteRepeatable(
                                animation =
                                tween(20000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart,
                            ),
                            label = "",
                        )

                Box(
                    modifier =
                    Modifier
                        .size(160.dp)
                        .offset(x = (index * 100).dp, y = (index * 80).dp)
                        .graphicsLayer {
                            rotationZ = rotation + index * 30
                            scaleX = 0.8f
                            scaleY = 0.8f
                        },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            // Enhanced header section
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 1f - (scrollState.value * 0.002f).coerceAtMost(0.3f)
                        translationY = -scrollState.value * 0.3f
                    },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier =
                        Modifier
                            .size(120.dp)
                            .shadow(20.dp, CircleShape)
                            .background(
                                brush =
                                Brush.radialGradient(
                                    colors =
                                    listOf(
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                            .copy(
                                                alpha =
                                                0.2f,
                                            ),
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                            .copy(
                                                alpha =
                                                0.1f,
                                            ),
                                    ),
                                ),
                                shape = CircleShape,
                            )
                            .padding(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoStories,
                            contentDescription = null,
                            modifier =
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    rotationZ = rotation * 2
                                },
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(modifier = Modifier.height(0.dp))
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = 2.sp,
                                ),
                            ) { append("Welcome to") }
                            append("\n\n") // Added extra newline for more spacing
                            withStyle(
                                SpanStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 52.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.sp,
                                    shadow =
                                    Shadow(
                                        color =
                                        MaterialTheme.colorScheme
                                            .primary.copy(
                                                alpha = 0.5f,
                                            ),
                                        offset = Offset(0f, 4f),
                                        blurRadius = 8f,
                                    ),
                                ),
                            ) { append("ReadRealm") }
                        },
                        textAlign = TextAlign.Center,
                        modifier =
                        Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .padding(vertical = 8.dp), // Added vertical padding
                    )

                    Text(
                        "Where Stories Come Alive", // Updated tagline
                        style =
                        MaterialTheme.typography.titleMedium.copy(
                            color =
                            MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.7f,
                            ),
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                        ),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            // Enhanced main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Enhanced login card
                Card(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(32.dp),
                            spotColor =
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.2f,
                            ),
                        )
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            brush =
                            Brush.verticalGradient(
                                colors =
                                listOf(
                                    MaterialTheme
                                        .colorScheme
                                        .surface
                                        .copy(
                                            alpha =
                                            0.95f,
                                        ),
                                    MaterialTheme
                                        .colorScheme
                                        .surface
                                        .copy(
                                            alpha =
                                            0.98f,
                                        ),
                                ),
                            ),
                        ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            "Sign In",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        MyTextField(
                            textFieldState = email,
                            onTextChange = { viewModel.onEmailChange(it) },
                            hint = "Email",
                            leadingIcon = Icons.Outlined.Email,
                            trailingIcon = Icons.Outlined.Check,
                            keyboardType = KeyboardType.Email,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        MyTextField(
                            textFieldState = password,
                            onTextChange = { viewModel.onPasswordChange(it) },
                            hint = "Password",
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { isChecked = it },
                                    colors =
                                    CheckboxDefaults.colors(
                                        checkedColor =
                                        MaterialTheme.colorScheme.primary,
                                    ),
                                )
                                Text("Remember me")
                            }
                            TextButton(onClick = { isSheetOpen = true }) {
                                Text("Forgot password?")
                            }
                        }

                        Button(
                            onClick = { viewModel.login(LoginRequest(email, password)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                "Login",
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }

                // Enhanced social login section
                Card(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor =
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.1f,
                            ),
                        ),
                    colors =
                    CardDefaults.cardColors(
                        containerColor =
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "Continue with",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            SocialLoginButton(
                                icon = R.drawable.google,
                                onClick = { /* Handle Google login */ },
                                backgroundColor = MaterialTheme.colorScheme.surface,
                            )
                            SocialLoginButton(
                                icon = R.drawable.facebook,
                                onClick = { /* Handle Facebook login */ },
                                backgroundColor = MaterialTheme.colorScheme.surface,
                            )
                            SocialLoginButton(
                                icon = R.drawable.instagram,
                                onClick = { /* Handle Instagram login */ },
                                backgroundColor = MaterialTheme.colorScheme.surface,
                            )
                        }
                    }
                }

                // Enhanced registration prompt
                Card(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor =
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.1f,
                            ),
                        ),
                    colors =
                    CardDefaults.cardColors(
                        containerColor =
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("New to BookHaven? ", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Join Now",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier =
                            Modifier
                                .clickable {
                                    navController.navigate(NavigationItem.Register.route)
                                }
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                },
                        )
                    }
                }
            }
        }
    }

    // Forgot password bottom sheet
    if (isSheetOpen) {
        ModalBottomSheet(sheetState = sheetState, onDismissRequest = { isSheetOpen = false }) {
            ForgotScreen(navController = navController)
        }
    }
}

@Composable
private fun SocialLoginButton(icon: Int, onClick: () -> Unit, backgroundColor: Color) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.92f else 1f, label = "")

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier =
        Modifier
            .size(65.dp)
            .shadow(
                elevation = if (isPressed) 4.dp else 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                )
            },
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}
