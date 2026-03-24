package tn.esprit.libraryapp.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import tn.esprit.libraryapp.NavigationItem
import tn.esprit.libraryapp.ui.theme.*
import tn.esprit.libraryapp.viewModel.AuthViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════════════════════════
// WIZARD'S GRIMOIRE - Profile as a Magical Character Sheet
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val viewModel: AuthViewModel = viewModel()
    val userProfile by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepLibraryBrown,
                        MysticPurple.copy(alpha = 0.3f),
                        DeepLibraryBrown
                    )
                )
            )
    ) {
        // Magical ambient particles
        MagicalAuraParticles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ═══════════════════════════════════════════════════════════════
            // HEADER - Mystical Portal with Character Portrait
            // ═══════════════════════════════════════════════════════════════
            WizardPortraitHeader(
                username = userProfile?.username ?: "Wanderer",
                profilePicture = userProfile?.profilePicture,
                onBackClick = { navController.navigateUp() },
                onLogoutClick = {
                    viewModel.logout()
                    navController.navigate(NavigationItem.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )

            // ═══════════════════════════════════════════════════════════════
            // READER'S RANK - Level & Experience System
            // ═══════════════════════════════════════════════════════════════
            ReaderRankCard(
                level = 12,
                currentXP = 2450,
                nextLevelXP = 3000,
                title = "Lore Keeper"
            )

            // ═══════════════════════════════════════════════════════════════
            // ACHIEVEMENT CONSTELLATION - Visual Achievement Map
            // ═══════════════════════════════════════════════════════════════
            AchievementConstellation()

            // ═══════════════════════════════════════════════════════════════
            // READING JOURNEY - Animated Stats Orbs
            // ═══════════════════════════════════════════════════════════════
            ReadingJourneyOrbs(
                booksRead = 47,
                hoursSpent = 156,
                currentStreak = 12,
                pagesRead = 8420
            )

            // ═══════════════════════════════════════════════════════════════
            // MAGICAL SCROLLS - Profile Actions as Ancient Scrolls
            // ═══════════════════════════════════════════════════════════════
            MagicalScrollActions(
                email = userProfile?.email ?: "unknown@realm.com",
                onEditProfile = { /* Navigate to edit */ },
                onChangePassword = { /* Navigate to change password */ },
                onSettings = { /* Navigate to settings */ }
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MAGICAL AURA PARTICLES
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun MagicalAuraParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val particles = remember {
        List(30) {
            AuraParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 6f + 2f,
                speed = Random.nextFloat() * 0.2f + 0.05f,
                color = if (Random.nextBoolean()) GildedGold else MysticPurple
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val animatedY = (particle.y + time * particle.speed) % 1f
            val animatedX = particle.x + sin(animatedY * PI * 3 + particle.x * 5).toFloat() * 0.03f
            val flickerAlpha = 0.3f + sin(time * PI * 6 + particle.x * 20).toFloat() * 0.2f

            drawCircle(
                color = particle.color.copy(alpha = flickerAlpha),
                radius = particle.size,
                center = Offset(animatedX * size.width, animatedY * size.height)
            )
        }
    }
}

private data class AuraParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val color: Color
)

// ═══════════════════════════════════════════════════════════════════════════════
// WIZARD PORTRAIT HEADER - Magical Character Display
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun WizardPortraitHeader(
    username: String,
    profilePicture: String?,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "portrait")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )
    
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        // Background mystical glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MysticPurple.copy(alpha = glowPulse * 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2, size.height * 0.6f),
                    radius = size.width * 0.6f
                ),
                center = Offset(size.width / 2, size.height * 0.6f)
            )
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button as magical rune
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(RichMahogany.copy(alpha = 0.6f))
                    .border(1.dp, GildedGold.copy(alpha = 0.5f), CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = AncientParchment,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Logout as portal exit
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF8B0000).copy(alpha = 0.6f),
                                RichMahogany.copy(alpha = 0.4f)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFFFF6B35).copy(alpha = 0.5f), CircleShape)
                    .clickable { onLogoutClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = CandlelightGlow,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Portrait with magical rings
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating ring
                Canvas(
                    modifier = Modifier
                        .size(160.dp)
                        .rotate(ringRotation)
                ) {
                    drawCircle(
                        color = GildedGold.copy(alpha = 0.3f),
                        style = Stroke(width = 2f),
                        radius = size.minDimension / 2
                    )
                    // Rune markers on ring
                    for (i in 0 until 8) {
                        val angle = (i * 45) * PI / 180
                        val x = center.x + cos(angle).toFloat() * (size.minDimension / 2)
                        val y = center.y + sin(angle).toFloat() * (size.minDimension / 2)
                        drawCircle(
                            color = GildedGold,
                            radius = 4f,
                            center = Offset(x, y)
                        )
                    }
                }

                // Inner counter-rotating ring
                Canvas(
                    modifier = Modifier
                        .size(140.dp)
                        .rotate(-ringRotation * 0.7f)
                ) {
                    drawCircle(
                        color = MysticPurple.copy(alpha = 0.4f),
                        style = Stroke(width = 1.5f),
                        radius = size.minDimension / 2
                    )
                }

                // Profile picture container
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MysticPurple.copy(alpha = 0.5f),
                                    DeepLibraryBrown
                                )
                            )
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    GildedGold,
                                    WarmLeather,
                                    GildedGold,
                                    CandlelightGlow,
                                    GildedGold
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    profilePicture?.let { base64Image ->
                        if (base64Image.isNotEmpty()) {
                                val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                        } else {
                            WizardPlaceholder()
                        }
                    } ?: WizardPlaceholder()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username with magical styling
            Text(
                text = username,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = AncientParchment
            )

            Text(
                text = "~ Reader of Realms ~",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic
                ),
                color = CandlelightGlow.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun WizardPlaceholder() {
    Icon(
        imageVector = Icons.Default.AutoStories,
        contentDescription = null,
        tint = GildedGold,
        modifier = Modifier.size(50.dp)
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// READER RANK CARD - RPG-Style Level System
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun ReaderRankCard(
    level: Int,
    currentXP: Int,
    nextLevelXP: Int,
    title: String
) {
    val progress = currentXP.toFloat() / nextLevelXP.toFloat()
    val infiniteTransition = rememberInfiniteTransition(label = "xp")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Card background with ornate border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            RichMahogany.copy(alpha = 0.7f),
                            MysticPurple.copy(alpha = 0.4f),
                            RichMahogany.copy(alpha = 0.7f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            GildedGold.copy(alpha = 0.6f),
                            CandlelightGlow.copy(alpha = 0.3f),
                            GildedGold.copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level Badge - Hexagonal Style
                Box(
                    modifier = Modifier.size(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            val radius = size.minDimension / 2
                            for (i in 0 until 6) {
                                val angle = (i * 60 - 90) * PI / 180
                                val x = center.x + cos(angle).toFloat() * radius
                                val y = center.y + sin(angle).toFloat() * radius
                                if (i == 0) moveTo(x, y) else lineTo(x, y)
                            }
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(GildedGold, WarmLeather)
                            )
                        )
                        drawPath(
                            path = path,
                            color = AncientParchment,
                            style = Stroke(width = 2f)
                        )
                    }
                    Text(
                        text = level.toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = DeepLibraryBrown
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = GildedGold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // XP Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DeepLibraryBrown.copy(alpha = 0.5f))
                    ) {
                        // Progress fill with shimmer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            GildedGold,
                                            CandlelightGlow,
                                            GildedGold
                                        ),
                                        startX = shimmer * 500 - 200,
                                        endX = shimmer * 500
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$currentXP / $nextLevelXP XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = CandlelightGlow.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ACHIEVEMENT CONSTELLATION - Visual Achievement Display
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun AchievementConstellation() {
    val achievements = listOf(
        Achievement("First Chapter", Icons.Outlined.MenuBook, true),
        Achievement("Week Warrior", Icons.Outlined.LocalFireDepartment, true),
        Achievement("Bookworm", Icons.Outlined.Star, true),
        Achievement("Night Owl", Icons.Outlined.Schedule, false),
        Achievement("Collector", Icons.Outlined.BookmarkBorder, true),
        Achievement("Legend", Icons.Outlined.EmojiEvents, false)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = GildedGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Achievement Constellation",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = AncientParchment
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(achievements) { achievement ->
                AchievementStar(achievement)
            }
        }
    }
}

private data class Achievement(
    val name: String,
    val icon: ImageVector,
    val unlocked: Boolean
)

@Composable
private fun AchievementStar(achievement: Achievement) {
    val infiniteTransition = rememberInfiniteTransition(label = "star")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .then(
                    if (achievement.unlocked) {
                        Modifier.background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GildedGold.copy(alpha = glow * 0.3f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                    } else Modifier
                )
                .border(
                    width = 2.dp,
                    color = if (achievement.unlocked) GildedGold else WarmLeather.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(
                    if (achievement.unlocked) {
                        Brush.radialGradient(
                            colors = listOf(
                                MysticPurple.copy(alpha = 0.6f),
                                DeepLibraryBrown
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                DeepLibraryBrown.copy(alpha = 0.8f),
                                DeepLibraryBrown
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = achievement.icon,
                contentDescription = achievement.name,
                tint = if (achievement.unlocked) GildedGold else WarmLeather.copy(alpha = 0.3f),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = achievement.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (achievement.unlocked) CandlelightGlow else WarmLeather.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// READING JOURNEY ORBS - Animated Statistics
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun ReadingJourneyOrbs(
    booksRead: Int,
    hoursSpent: Int,
    currentStreak: Int,
    pagesRead: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Your Reading Journey",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = AncientParchment
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FloatingStatOrb(
                value = booksRead.toString(),
                label = "Books",
                icon = Icons.Default.MenuBook,
                color = GildedGold,
                delay = 0
            )
            FloatingStatOrb(
                value = "${hoursSpent}h",
                label = "Reading",
                icon = Icons.Default.Schedule,
                color = CandlelightGlow,
                delay = 200
            )
            FloatingStatOrb(
                value = currentStreak.toString(),
                label = "Streak",
                icon = Icons.Default.LocalFireDepartment,
                color = Color(0xFFFF6B35),
                delay = 400
            )
            FloatingStatOrb(
                value = "${pagesRead/1000}k",
                label = "Pages",
                icon = Icons.Default.Description,
                color = MysticPurple,
                delay = 600
            )
        }
    }
}

@Composable
private fun FloatingStatOrb(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    delay: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb$label")
    val float by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000 + delay, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.offset(y = float.dp)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = glow),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.3f),
                                DeepLibraryBrown.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = color.copy(alpha = 0.6f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AncientParchment
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CandlelightGlow.copy(alpha = 0.7f)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MAGICAL SCROLL ACTIONS - Profile Actions as Ancient Scrolls
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun MagicalScrollActions(
    email: String,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Email display as mystical inscription
        ScrollActionItem(
            icon = Icons.Default.Email,
            title = "Magical Address",
            subtitle = email,
            onClick = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ScrollActionItem(
            icon = Icons.Default.Edit,
            title = "Edit Your Chronicle",
            subtitle = "Modify your wizard profile",
            onClick = onEditProfile
        )

        Spacer(modifier = Modifier.height(12.dp))

        ScrollActionItem(
            icon = Icons.Default.Lock,
            title = "Ward Your Secrets",
            subtitle = "Change magical password",
            onClick = onChangePassword
        )

        Spacer(modifier = Modifier.height(12.dp))

        ScrollActionItem(
            icon = Icons.Default.Settings,
            title = "Arcane Settings",
            subtitle = "Configure your realm",
            onClick = onSettings
        )
    }
}

@Composable
private fun ScrollActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        RichMahogany.copy(alpha = 0.5f),
                        MysticPurple.copy(alpha = 0.2f),
                        RichMahogany.copy(alpha = 0.5f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = WarmLeather.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GildedGold.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.dp, GildedGold.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GildedGold,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AncientParchment
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = CandlelightGlow.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = GildedGold.copy(alpha = 0.6f)
            )
        }
    }
}
