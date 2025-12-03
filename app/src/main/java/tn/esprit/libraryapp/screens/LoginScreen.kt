package tn.esprit.libraryapp.screens

import android.widget.Toast
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
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
import tn.esprit.libraryapp.models.LoginRequest
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
private val InkBlue = Color(0xFF1B2838)

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
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    LaunchedEffect(loginResult) {
        loginResult?.let {
            if (it.isSuccess) {
                Toast.makeText(context, "Welcome to ReadRealm!", Toast.LENGTH_SHORT).show()
                viewModel.clearLoginResult()
                navController.navigate(NavigationItem.Home.route) {
                    popUpTo(NavigationItem.Login.route) { inclusive = true }
                }
            } else {
                Toast.makeText(context, "The realm remains sealed...", Toast.LENGTH_SHORT).show()
                viewModel.clearLoginResult()
            }
        }
    }

    // Magical Animations
    val infiniteTransition = rememberInfiniteTransition(label = "magical_effects")
    
    val candleFlicker by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "candle_flicker",
    )

    val magicDust by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "magic_dust",
    )

    val bookFloat by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "book_float",
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
        // Enchanted Library Background
        EnchantedLibraryBackground(
            candleFlicker = candleFlicker,
            magicDust = magicDust,
        )

        // Floating Magic Particles
        MagicalParticles(rotation = magicDust)

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.08f))

            // Magical Book Portal Header
            MagicalBookHeader(
                bookFloat = bookFloat,
                glowIntensity = glowIntensity,
                candleFlicker = candleFlicker,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ancient Tome Login Card
            AncientTomeCard(
                email = email,
                password = password,
                isChecked = isChecked,
                onEmailChange = { viewModel.onEmailChange(it) },
                onPasswordChange = { viewModel.onPasswordChange(it) },
                onCheckedChange = { isChecked = it },
                onForgotPassword = { isSheetOpen = true },
                onLogin = { viewModel.login(LoginRequest(email, password)) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mystical Divider
            MysticalDivider()

            Spacer(modifier = Modifier.height(20.dp))

            // Social Login Runes
            SocialLoginRunes()

            Spacer(modifier = Modifier.height(28.dp))

            // Join the Realm Prompt
            JoinRealmPrompt(
                glowIntensity = glowIntensity,
                onJoinClick = { navController.navigate(NavigationItem.Register.route) },
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Forgot password bottom sheet
    if (isSheetOpen) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen = false },
            containerColor = AncientParchment,
            scrimColor = DeepLibraryBrown.copy(alpha = 0.7f),
        ) {
            ForgotScreen(navController = navController)
        }
    }
}

@Composable
private fun EnchantedLibraryBackground(
    candleFlicker: Float,
    magicDust: Float,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Deep library gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DeepLibraryBrown,
                            Color(0xFF0D0705),
                            Color(0xFF251520), // Muted purple-brown
                            Color(0xFF1A2028), // Muted ink blue
                        ),
                    ),
                ),
        )

        // Candlelight glow effect at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CandlelightGlow.copy(alpha = 0.15f * candleFlicker),
                            Color.Transparent,
                        ),
                        center = Offset(500f, 100f),
                        radius = 600f,
                    ),
                ),
        )

        // Secondary warm glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .offset(y = 100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GildedGold.copy(alpha = 0.08f * candleFlicker),
                            Color.Transparent,
                        ),
                        center = Offset(200f, 300f),
                        radius = 500f,
                    ),
                ),
        )

        // Vignette overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f),
                        ),
                        radius = 1000f,
                    ),
                ),
        )

        // Bookshelf shadows on edges
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f),
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun MagicalParticles(rotation: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val particleCount = 30
        for (i in 0 until particleCount) {
            val angle = (rotation + i * (360f / particleCount)) * (Math.PI / 180f)
            val radius = 200f + (i % 5) * 80f
            val x = size.width / 2 + (cos(angle) * radius).toFloat()
            val y = size.height / 3 + (sin(angle) * radius * 0.5f).toFloat()
            
            val particleSize = (2f + (i % 3) * 1.5f)
            val alpha = (0.2f + (i % 4) * 0.1f).coerceIn(0f, 0.6f)
            
            drawCircle(
                color = GildedGold.copy(alpha = alpha),
                radius = particleSize,
                center = Offset(x, y),
            )
        }
    }
}

@Composable
private fun MagicalBookHeader(
    bookFloat: Float,
    glowIntensity: Float,
    candleFlicker: Float,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 24.dp),
    ) {
        // Magical floating book icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.offset(y = bookFloat.dp),
        ) {
            // Outer magical glow
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .blur(40.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GildedGold.copy(alpha = glowIntensity * 0.4f),
                                MysticPurple.copy(alpha = glowIntensity * 0.2f),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )

            // Inner glow ring
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                GildedGold.copy(alpha = 0.6f),
                                Color.Transparent,
                                GildedGold.copy(alpha = 0.3f),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )

            // Book icon container
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = CircleShape,
                        spotColor = GildedGold.copy(alpha = 0.5f),
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
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoStories,
                    contentDescription = "ReadRealm",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = 0.9f + (candleFlicker * 0.1f)
                            scaleY = 0.9f + (candleFlicker * 0.1f)
                        },
                    tint = CandlelightGlow,
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Title with magical styling
        Text(
            text = "ReadRealm",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                shadow = Shadow(
                    color = GildedGold.copy(alpha = 0.8f),
                    offset = Offset(0f, 4f),
                    blurRadius = 16f,
                ),
            ),
            color = AncientParchment,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "— Enter the Realm of Stories —",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontStyle = FontStyle.Italic,
                letterSpacing = 2.sp,
            ),
            color = GildedGold.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun AncientTomeCard(
    email: String,
    password: String,
    isChecked: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onForgotPassword: () -> Unit,
    onLogin: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 32.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.6f),
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
            // Decorative corner ornaments
            Box(modifier = Modifier.fillMaxWidth()) {
                // Top left ornament
                OrnamentCorner(
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                )
                // Top right ornament
                OrnamentCorner(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .rotate(90f),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // Header with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Key,
                        contentDescription = null,
                        tint = RichMahogany,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Unlock Your Story",
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
                        hint = "Enter your secret phrase",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Remember me & Forgot password
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = onCheckedChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = RichMahogany,
                                uncheckedColor = WarmLeather.copy(alpha = 0.6f),
                                checkmarkColor = CandlelightGlow,
                            ),
                        )
                        Text(
                            "Remember my tome",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepLibraryBrown.copy(alpha = 0.8f),
                        )
                    }
                    TextButton(onClick = onForgotPassword) {
                        Text(
                            "Lost the key?",
                            color = RichMahogany,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                // Login button
                Button(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = RichMahogany),
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
                                imageVector = Icons.Filled.MenuBook,
                                contentDescription = null,
                                tint = CandlelightGlow,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Open the Tome",
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
private fun OrnamentCorner(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val color = GildedGold.copy(alpha = 0.6f)
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 2f,
        )
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 2f,
        )
        drawCircle(
            color = color,
            radius = 4f,
            center = Offset(4f, 4f),
        )
    }
}

@Composable
private fun MysticalDivider() {
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
                            GildedGold.copy(alpha = 0.5f),
                        ),
                    ),
                ),
        )
        
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(8.dp)
                .rotate(45f)
                .background(GildedGold.copy(alpha = 0.6f)),
        )
        
        Text(
            "or use ancient portals",
            style = MaterialTheme.typography.bodySmall.copy(
                fontStyle = FontStyle.Italic,
            ),
            color = AncientParchment.copy(alpha = 0.7f),
        )
        
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(8.dp)
                .rotate(45f)
                .background(GildedGold.copy(alpha = 0.6f)),
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            GildedGold.copy(alpha = 0.5f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun SocialLoginRunes() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        SocialRuneButton(
            icon = R.drawable.google,
            onClick = { },
        )
        SocialRuneButton(
            icon = R.drawable.facebook,
            onClick = { },
        )
        SocialRuneButton(
            icon = R.drawable.instagram,
            onClick = { },
        )
    }
}

@Composable
private fun SocialRuneButton(icon: Int, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f, label = "")

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = AncientParchment.copy(alpha = 0.95f),
        modifier = Modifier
            .size(70.dp)
            .shadow(
                elevation = if (isPressed) 4.dp else 16.dp,
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
                        GildedGold.copy(alpha = 0.6f),
                        WarmLeather.copy(alpha = 0.4f),
                        GildedGold.copy(alpha = 0.6f),
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
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun JoinRealmPrompt(
    glowIntensity: Float,
    onJoinClick: () -> Unit,
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
                        MysticPurple.copy(alpha = 0.15f),
                        Color.Transparent,
                    ),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        GildedGold.copy(alpha = 0.4f * glowIntensity),
                        Color.Transparent,
                    ),
                ),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "New to the Realm? ",
                style = MaterialTheme.typography.bodyLarge,
                color = AncientParchment.copy(alpha = 0.8f),
            )
            Text(
                "Begin Your Journey",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = GildedGold,
                modifier = Modifier
                    .clickable(onClick = onJoinClick)
                    .graphicsLayer {
                        scaleX = 1f + (glowIntensity - 0.5f) * 0.1f
                        scaleY = 1f + (glowIntensity - 0.5f) * 0.1f
                    },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = GildedGold,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
