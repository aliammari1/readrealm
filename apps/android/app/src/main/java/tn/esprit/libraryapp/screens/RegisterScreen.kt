package tn.esprit.libraryapp.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
import coil3.compose.rememberAsyncImagePainter
import tn.esprit.libraryapp.NavigationItem
import tn.esprit.libraryapp.R
import tn.esprit.libraryapp.components.MyTextField
import tn.esprit.libraryapp.models.RegisterRequest
import tn.esprit.libraryapp.viewModel.AuthViewModel
import java.io.ByteArrayOutputStream
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
val EnchantedGreen = Color(0xFF1B4332)
private val InkBlue = Color(0xFF1B2838)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val viewModel: AuthViewModel = viewModel()
    val name by viewModel.name.collectAsState("")
    val email by viewModel.email.collectAsState("")
    val password by viewModel.password.collectAsState("")
    val registerResult by viewModel.registerResult.collectAsState()
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Convert Uri to Base64
    fun uriToBase64(uri: Uri): String {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    LaunchedEffect(registerResult) {
        registerResult?.let {
            if (it.isSuccess) {
                Toast.makeText(context, "Welcome to the Realm, new scribe!", Toast.LENGTH_SHORT).show()
                viewModel.clearRegisterResult()
                navController.navigate(NavigationItem.Login.route)
            } else {
                Toast.makeText(context, "The ancient scrolls rejected your entry...", Toast.LENGTH_SHORT).show()
                viewModel.clearRegisterResult()
            }
        }
    }

    // Magical Animations
    val infiniteTransition = rememberInfiniteTransition(label = "register_animations")

    val candleFlicker by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "candle_flicker",
    )

    val magicDust by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "magic_dust",
    )

    val portalPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "portal_pulse",
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

    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        // Enchanted Background with different mystical atmosphere
        EnchantedRegistrationBackground(
            candleFlicker = candleFlicker,
        )

        // Magical Particles
        RegistrationMagicalParticles(rotation = magicDust)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.05f))

            // Mystical Portal Profile Picture
            MysticalPortalHeader(
                imageUri = imageUri,
                portalPulse = portalPulse,
                glowIntensity = glowIntensity,
                candleFlicker = candleFlicker,
                onImageClick = { launcher.launch("image/*") },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Registration Form
            AncientScrollForm(
                name = name,
                email = email,
                password = password,
                onNameChange = { viewModel.onNameChange(it) },
                onEmailChange = { viewModel.onEmailChange(it) },
                onPasswordChange = { viewModel.onPasswordChange(it) },
                onRegister = {
                    val base64Image = imageUri?.let { uriToBase64(it) }
                    viewModel.register(
                        RegisterRequest(
                            username = name,
                            email = email,
                            password = password,
                            profilePicture = base64Image,
                        ),
                    )
                },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mystical Divider
            MysticalRegistrationDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // Social Sign Up Runes
            SocialSignUpRunes()

            Spacer(modifier = Modifier.height(24.dp))

            // Already a member prompt
            AlreadyMemberPrompt(
                glowIntensity = glowIntensity,
                onSignInClick = { navController.navigate(NavigationItem.Login.route) },
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EnchantedRegistrationBackground(
    candleFlicker: Float,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Deeper mystical gradient with purple/green undertones
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DeepLibraryBrown,
                            Color(0xFF2A1A28), // Muted purple-brown
                            Color(0xFF1A2A20), // Muted green-brown
                            Color(0xFF0D0705),
                        ),
                    ),
                ),
        )

        // Multiple mystical light sources
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MysticPurple.copy(alpha = 0.2f * candleFlicker),
                            Color.Transparent,
                        ),
                        center = Offset(300f, 200f),
                        radius = 500f,
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .offset(y = 150.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GildedGold.copy(alpha = 0.1f * candleFlicker),
                            Color.Transparent,
                        ),
                        center = Offset(600f, 300f),
                        radius = 450f,
                    ),
                ),
        )

        // Enhanced vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f),
                        ),
                        radius = 900f,
                    ),
                ),
        )

        // Side shadows for depth
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f),
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun RegistrationMagicalParticles(rotation: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val particleCount = 35
        for (i in 0 until particleCount) {
            val angle = (rotation * 0.8f + i * (360f / particleCount)) * (Math.PI / 180f)
            val radius = 180f + (i % 6) * 70f
            val x = size.width / 2 + (cos(angle) * radius).toFloat()
            val y = size.height / 4 + (sin(angle) * radius * 0.6f).toFloat()

            val particleSize = (2f + (i % 4) * 1.2f)
            val alpha = (0.15f + (i % 5) * 0.08f).coerceIn(0f, 0.5f)

            // Alternating colors for magical effect
            val color = if (i % 2 == 0) GildedGold else MysticPurple

            drawCircle(
                color = color.copy(alpha = alpha),
                radius = particleSize,
                center = Offset(x, y),
            )
        }
    }
}

@Composable
private fun MysticalPortalHeader(
    imageUri: Uri?,
    portalPulse: Float,
    glowIntensity: Float,
    candleFlicker: Float,
    onImageClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 24.dp),
    ) {
        // Title
        Text(
            text = "Begin Your Legend",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                shadow = Shadow(
                    color = GildedGold.copy(alpha = 0.7f),
                    offset = Offset(0f, 3f),
                    blurRadius = 12f,
                ),
            ),
            color = AncientParchment,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "— Inscribe Your Name in the Annals —",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.sp,
            ),
            color = GildedGold.copy(alpha = 0.7f),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mystical Portal for Profile Picture
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.clickable(onClick = onImageClick),
        ) {
            // Outer mystical portal ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .blur(25.dp)
                    .graphicsLayer { scaleX = portalPulse; scaleY = portalPulse }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MysticPurple.copy(alpha = glowIntensity * 0.5f),
                                GildedGold.copy(alpha = glowIntensity * 0.3f),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )

            // Rotating rune ring
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .rotate(portalPulse * 10f)
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                GildedGold.copy(alpha = 0.7f),
                                MysticPurple.copy(alpha = 0.5f),
                                Color.Transparent,
                                GildedGold.copy(alpha = 0.4f),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )

            // Profile picture container
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        spotColor = MysticPurple.copy(alpha = 0.6f),
                    )
                    .background(
                        brush = Brush.linearGradient(
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
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Your Portrait",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddAPhoto,
                            contentDescription = "Add Portrait",
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    scaleX = 0.9f + (candleFlicker * 0.1f)
                                    scaleY = 0.9f + (candleFlicker * 0.1f)
                                },
                            tint = CandlelightGlow,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Portrait",
                            style = MaterialTheme.typography.labelSmall,
                            color = CandlelightGlow.copy(alpha = 0.8f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AncientScrollForm(
    name: String,
    email: String,
    password: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 32.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color.Black.copy(alpha = 0.6f),
            ),
        shape = RoundedCornerShape(28.dp),
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
                    shape = RoundedCornerShape(28.dp),
                ),
        ) {
            // Decorative corners
            Box(modifier = Modifier.fillMaxWidth()) {
                ScrollOrnament(modifier = Modifier.align(Alignment.TopStart).padding(12.dp))
                ScrollOrnament(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).rotate(90f))
                ScrollOrnament(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp).rotate(270f))
                ScrollOrnament(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).rotate(180f))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.HistoryEdu,
                        contentDescription = null,
                        tint = RichMahogany,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Scribe Your Details",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        ),
                        color = DeepLibraryBrown,
                    )
                }

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

                // Name field
                Column {
                    Text(
                        "Your Name (How shall we call you?)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = RichMahogany,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
                    )
                    MyTextField(
                        textFieldState = name,
                        onTextChange = onNameChange,
                        hint = "Enter your name",
                        leadingIcon = Icons.Outlined.AccountCircle,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Email field
                Column {
                    Text(
                        "Scribe's Mark (Email)",
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

                // Password field
                Column {
                    Text(
                        "Secret Incantation (Password)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = RichMahogany,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
                    )
                    MyTextField(
                        textFieldState = password,
                        onTextChange = onPasswordChange,
                        hint = "Create a powerful phrase",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Register button
                Button(
                    onClick = onRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = MysticPurple),
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
                                        Color(0xFF3A2040), // Purple-mahogany blend
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
                                imageVector = Icons.Filled.AutoStories,
                                contentDescription = null,
                                tint = CandlelightGlow,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Enter the Realm",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
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
private fun ScrollOrnament(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val color = GildedGold.copy(alpha = 0.5f)
        // Corner flourish
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width * 0.8f, 0f),
            strokeWidth = 2f,
        )
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height * 0.8f),
            strokeWidth = 2f,
        )
        drawCircle(
            color = color,
            radius = 3f,
            center = Offset(3f, 3f),
        )
    }
}

@Composable
private fun MysticalRegistrationDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GildedGold.copy(alpha = 0.4f),
                        ),
                    ),
                ),
        )

        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = GildedGold.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "or summon via",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                ),
                color = AncientParchment.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = GildedGold.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp),
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            GildedGold.copy(alpha = 0.4f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun SocialSignUpRunes() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        SocialRuneRegisterButton(icon = R.drawable.google, onClick = { })
        SocialRuneRegisterButton(icon = R.drawable.facebook, onClick = { })
        SocialRuneRegisterButton(icon = R.drawable.instagram, onClick = { })
    }
}

@Composable
private fun SocialRuneRegisterButton(icon: Int, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f, label = "")

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = AncientParchment.copy(alpha = 0.95f),
        modifier = Modifier
            .size(65.dp)
            .shadow(
                elevation = if (isPressed) 4.dp else 14.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.4f),
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
            }
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GildedGold.copy(alpha = 0.5f),
                        MysticPurple.copy(alpha = 0.3f),
                        GildedGold.copy(alpha = 0.5f),
                    ),
                ),
                shape = RoundedCornerShape(16.dp),
            ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun AlreadyMemberPrompt(
    glowIntensity: Float,
    onSignInClick: () -> Unit,
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
                        EnchantedGreen.copy(alpha = 0.1f),
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
                "Already a member? ",
                style = MaterialTheme.typography.bodyLarge,
                color = AncientParchment.copy(alpha = 0.8f),
            )
            Text(
                "Open Your Tome",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = GildedGold,
                modifier = Modifier
                    .clickable(onClick = onSignInClick)
                    .graphicsLayer {
                        scaleX = 1f + (glowIntensity - 0.5f) * 0.08f
                        scaleY = 1f + (glowIntensity - 0.5f) * 0.08f
                    },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.MenuBook,
                contentDescription = null,
                tint = GildedGold,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
