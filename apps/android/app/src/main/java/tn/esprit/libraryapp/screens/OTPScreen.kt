package tn.esprit.libraryapp.screens

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.esprit.libraryapp.models.VerifyEmailRequest
import tn.esprit.libraryapp.viewModel.AuthViewModel
import kotlin.math.cos
import kotlin.math.sin

// Immersive Library Theme Colors
private val DeepLibraryBrown = Color(0xFF1A0F0A)
private val RichMahogany = Color(0xFF4A2C2A)
private val WarmLeather = Color(0xFF8B5A2B)
private val GildedGold = Color(0xFFD4AF37)
private val AncientParchment = Color(0xFFF5E6C8)
private val CandlelightGlow = Color(0xFFFFE4B5)
private val MysticPurple = Color(0xFF2D1B4E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    navController: NavHostController,
    email: String,
) {
    val viewModel: AuthViewModel = viewModel()
    val verificationCode = remember { mutableStateListOf<String>().apply { repeat(6) { add("") } } }
    val verifyEmailResult by viewModel.verifyEmailResult.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    // Magical Animations
    val infiniteTransition = rememberInfiniteTransition(label = "otp_animations")

    val runeGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "rune_glow",
    )

    val magicCircle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "magic_circle",
    )

    Box(
        modifier = Modifier
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
            // Magic Shield Icon
            Box(contentAlignment = Alignment.Center) {
                // Rotating magic circle
                Canvas(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer { rotationZ = magicCircle },
                ) {
                    val particleCount = 12
                    for (i in 0 until particleCount) {
                        val angle = (i * (360f / particleCount)) * (Math.PI / 180f)
                        val radius = size.minDimension / 2 - 10
                        val x = center.x + (cos(angle) * radius).toFloat()
                        val y = center.y + (sin(angle) * radius).toFloat()

                        drawCircle(
                            color = GildedGold.copy(alpha = runeGlow * 0.6f),
                            radius = 4f,
                            center = Offset(x, y),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .shadow(12.dp, CircleShape, spotColor = GildedGold)
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
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = CandlelightGlow,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = "Sacred Incantation",
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
                text = "Enter the mystical runes sent to your scribe's mark",
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

            Spacer(modifier = Modifier.height(24.dp))

            // Rune Code Input Fields
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                for (i in 0 until 6) {
                    RuneInputField(
                        value = verificationCode[i],
                        onValueChange = { newValue ->
                            if (newValue.length <= 1) {
                                verificationCode[i] = newValue
                            }
                        },
                        runeGlow = runeGlow,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Verify Button
            Button(
                onClick = {
                    val code = verificationCode.joinToString("")
                    viewModel.verifyEmail(VerifyEmailRequest(email, code))
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
                            imageVector = Icons.Filled.LockOpen,
                            contentDescription = null,
                            tint = CandlelightGlow,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Unlock the Seal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            ),
                            color = CandlelightGlow,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    verifyEmailResult?.let {
        if (it.isSuccess) {
            Toast.makeText(context, "The seal is broken! Access granted.", Toast.LENGTH_SHORT).show()
            isSheetOpen = true
        } else {
            Toast.makeText(context, "The runes are incorrect...", Toast.LENGTH_SHORT).show()
        }
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen = false },
            containerColor = AncientParchment,
            contentColor = DeepLibraryBrown,
        ) {
            ResetPasswordScreen(
                navController = navController,
                email = email,
            )
        }
    }
}

@Composable
private fun RuneInputField(
    value: String,
    onValueChange: (String) -> Unit,
    runeGlow: Float,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        maxLines = 1,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
            .height(60.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = GildedGold.copy(alpha = runeGlow * 0.3f),
            ),
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.headlineMedium.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = DeepLibraryBrown,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CandlelightGlow.copy(alpha = 0.5f),
            unfocusedContainerColor = AncientParchment,
            focusedBorderColor = GildedGold,
            unfocusedBorderColor = WarmLeather.copy(alpha = 0.5f),
            cursorColor = RichMahogany,
        ),
    )
}
