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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.esprit.libraryapp.NavigationItem
import tn.esprit.libraryapp.components.MyTextField
import tn.esprit.libraryapp.models.GenerateEmailRequest
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
fun ForgotScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val viewModel: AuthViewModel = viewModel()
    val email = remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()
    var isSheetopen by rememberSaveable { mutableStateOf(false) }
    val generateEmailResult by viewModel.generateEmailResult.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Magical Animations
    val infiniteTransition = rememberInfiniteTransition(label = "forgot_animations")

    val candleFlicker by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "candle_flicker",
    )

    val keyFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "key_float",
    )

    val magicDust by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "magic_dust",
    )

    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_intensity",
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Enchanted Background
        ForgotEnchantedBackground(candleFlicker = candleFlicker)

        // Floating dust particles
        ForgotMagicalParticles(rotation = magicDust)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.08f))

            // Mystical Key Header
            MysticalKeyHeader(
                keyFloat = keyFloat,
                glowIntensity = glowIntensity,
                candleFlicker = candleFlicker,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Ancient Scroll Form
            LostKeyScrollForm(
                email = email.value,
                onEmailChange = { email.value = it },
                onSendCode = { viewModel.generateEmail(GenerateEmailRequest(email.value)) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Remember Password Prompt
            RememberPasswordPrompt(
                glowIntensity = glowIntensity,
                onLoginClick = { navController.navigate(NavigationItem.Login.route) },
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Handle result
        generateEmailResult?.let {
            if (it.isSuccess) {
                Toast.makeText(context, "Ancient scroll sent to your mark!", Toast.LENGTH_SHORT).show()
                isSheetopen = true
            } else {
                Toast.makeText(context, "The owl couldn't find your mark...", Toast.LENGTH_SHORT).show()
            }
        }

        if (isSheetopen) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { isSheetopen = false },
                containerColor = AncientParchment,
                contentColor = DeepLibraryBrown,
            ) {
                OtpScreen(
                    navController = navController,
                    email = email.value,
                )
            }
        }
    }
}

@Composable
private fun ForgotEnchantedBackground(candleFlicker: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Deep mystical gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DeepLibraryBrown,
                            Color(0xFF251520), // Muted purple-brown
                            Color(0xFF0D0705),
                        ),
                    ),
                ),
        )

        // Candlelight glow from top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CandlelightGlow.copy(alpha = 0.12f * candleFlicker),
                            Color.Transparent,
                        ),
                        center = Offset(500f, 150f),
                        radius = 500f,
                    ),
                ),
        )

        // Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f),
                        ),
                        radius = 900f,
                    ),
                ),
        )
    }
}

@Composable
private fun ForgotMagicalParticles(rotation: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val particleCount = 25
        for (i in 0 until particleCount) {
            val angle = (rotation + i * (360f / particleCount)) * (Math.PI / 180f)
            val radius = 150f + (i % 5) * 60f
            val x = size.width / 2 + (cos(angle) * radius).toFloat()
            val y = size.height / 3 + (sin(angle) * radius * 0.5f).toFloat()

            val particleSize = (1.5f + (i % 3) * 1f)
            val alpha = (0.1f + (i % 4) * 0.06f).coerceIn(0f, 0.4f)

            drawCircle(
                color = GildedGold.copy(alpha = alpha),
                radius = particleSize,
                center = Offset(x, y),
            )
        }
    }
}

@Composable
private fun MysticalKeyHeader(
    keyFloat: Float,
    glowIntensity: Float,
    candleFlicker: Float,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 24.dp),
    ) {
        // Floating Key Icon
        Box(contentAlignment = Alignment.Center) {
            // Glow behind key
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .blur(30.dp)
                    .offset(y = keyFloat.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GildedGold.copy(alpha = glowIntensity * 0.5f),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(y = keyFloat.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        spotColor = GildedGold.copy(alpha = 0.5f),
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                RichMahogany,
                                WarmLeather,
                                RichMahogany,
                            ),
                        ),
                        shape = CircleShape,
                    )
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                GildedGold,
                                GildedGold.copy(alpha = 0.5f),
                                GildedGold,
                            ),
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Key,
                    contentDescription = null,
                    tint = CandlelightGlow,
                    modifier = Modifier
                        .size(48.dp)
                        .rotate(-30f)
                        .graphicsLayer {
                            scaleX = 0.95f + (candleFlicker * 0.05f)
                            scaleY = 0.95f + (candleFlicker * 0.05f)
                        },
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "Lost Your Key?",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                shadow = Shadow(
                    color = GildedGold.copy(alpha = 0.6f),
                    offset = Offset(0f, 3f),
                    blurRadius = 12f,
                ),
            ),
            color = AncientParchment,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Fear not, every locked tome has a spare incantation",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic,
                letterSpacing = 0.5.sp,
            ),
            color = GildedGold.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
private fun LostKeyScrollForm(
    email: String,
    onEmailChange: (String) -> Unit,
    onSendCode: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.5f),
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AncientParchment,
                            AncientParchment.copy(alpha = 0.95f),
                            Color(0xFFE8D4B8),
                        ),
                    ),
                )
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GildedGold,
                            WarmLeather,
                            GildedGold,
                        ),
                    ),
                    shape = RoundedCornerShape(24.dp),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = RichMahogany,
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Summon a New Key",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        ),
                        color = DeepLibraryBrown,
                    )
                }

                // Description
                Text(
                    "Enter the mark inscribed upon your account, and we shall send an enchanted scroll to restore your passage.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RichMahogany.copy(alpha = 0.8f),
                )

                // Decorative line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    WarmLeather.copy(alpha = 0.5f),
                                    GildedGold.copy(alpha = 0.7f),
                                    WarmLeather.copy(alpha = 0.5f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )

                // Email field
                Column {
                    Text(
                        "Your Scribe's Mark",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = RichMahogany,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
                    )
                    MyTextField(
                        textFieldState = email,
                        onTextChange = onEmailChange,
                        hint = "your.name@realm.com",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardType = KeyboardType.Email,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Send Code Button
                Button(
                    onClick = onSendCode,
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
                                imageVector = Icons.Filled.Send,
                                contentDescription = null,
                                tint = CandlelightGlow,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Send Enchanted Scroll",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                ),
                                color = CandlelightGlow,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RememberPasswordPrompt(
    glowIntensity: Float,
    onLoginClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        WarmLeather.copy(alpha = 0.1f),
                        Color.Transparent,
                    ),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        GildedGold.copy(alpha = 0.3f * glowIntensity),
                        Color.Transparent,
                    ),
                ),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Found your key? ",
                style = MaterialTheme.typography.bodyLarge,
                color = AncientParchment.copy(alpha = 0.8f),
            )
            Text(
                "Return to Gate",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = GildedGold,
                modifier = Modifier
                    .clickable(onClick = onLoginClick)
                    .graphicsLayer {
                        scaleX = 1f + (glowIntensity - 0.5f) * 0.05f
                        scaleY = 1f + (glowIntensity - 0.5f) * 0.05f
                    },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.MenuBook,
                contentDescription = null,
                tint = GildedGold,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
