package tn.esprit.libraryapp.screens.experience

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Theme colors
private val DeepLibraryBrown = Color(0xFF2C1810)
private val GildedGold = Color(0xFFD4AF37)
private val MysticPurple = Color(0xFF4A1942)
private val AncientParchment = Color(0xFFF5E6D3)

/**
 * Loading screen while experience is being generated
 */
@Composable
fun ExperienceLoadingScreen(
    bookTitle: String,
    loadingProgress: Float,
    loadingMessage: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    // Pulsing animation
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Rotation animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(MysticPurple, DeepLibraryBrown, Color(0xFF0a0a15))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Magical particles background
        MagicalParticlesBackground()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated magic circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                // Outer glow ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = size.minDimension / 2 - 20f

                    // Draw rotating magic symbols (represented as circles)
                    for (i in 0 until 8) {
                        val angle = Math.toRadians((rotation + i * 45).toDouble())
                        val x = centerX + radius * cos(angle).toFloat()
                        val y = centerY + radius * sin(angle).toFloat()

                        drawCircle(
                            color = GildedGold.copy(alpha = pulseAlpha * 0.8f),
                            radius = 8f,
                            center = Offset(x, y)
                        )
                    }

                    // Inner ring
                    drawCircle(
                        color = GildedGold.copy(alpha = 0.3f),
                        radius = radius * 0.7f,
                        center = Offset(centerX, centerY),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }

                // Center icon
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = GildedGold.copy(alpha = pulseAlpha),
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Crafting Your Experience",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = GildedGold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"$bookTitle\"",
                style = MaterialTheme.typography.titleMedium,
                fontStyle = FontStyle.Italic,
                color = AncientParchment.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MysticPurple.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(loadingProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        GildedGold.copy(alpha = 0.5f),
                                        GildedGold,
                                        GildedGold.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = loadingMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AncientParchment.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${(loadingProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = GildedGold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Tips
            LoadingTip()
        }
    }
}

@Composable
private fun MagicalParticlesBackground() {
    val particles = remember { List(30) { MagicParticle() } }
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val yPos = ((1f - time) * size.height * 1.5f + particle.startY) % (size.height * 1.5f)
            val xPos = particle.startX * size.width + sin(time * 6.28f * particle.drift) * 50f
            val fadeAlpha = when {
                yPos < size.height * 0.1f -> yPos / (size.height * 0.1f)
                yPos > size.height * 0.9f -> (size.height - yPos) / (size.height * 0.1f)
                else -> 1f
            }.coerceIn(0f, 1f)

            drawCircle(
                color = particle.color.copy(alpha = fadeAlpha * particle.alpha),
                radius = particle.size,
                center = Offset(xPos, yPos)
            )
        }
    }
}

private data class MagicParticle(
    val startX: Float = Random.nextFloat(),
    val startY: Float = Random.nextFloat() * 1000f,
    val size: Float = Random.nextFloat() * 4f + 2f,
    val drift: Float = Random.nextFloat() * 0.5f + 0.2f,
    val alpha: Float = Random.nextFloat() * 0.5f + 0.3f,
    val color: Color = listOf(
        Color(0xFFD4AF37),
        Color(0xFF9C27B0),
        Color(0xFF00BCD4),
        Color(0xFFFFFFFF)
    ).random()
)

@Composable
private fun LoadingTip() {
    val tips = listOf(
        "Explore every corner to discover hidden secrets",
        "Talk to characters to uncover their stories",
        "Key scenes reveal the heart of the narrative",
        "The map shows connections between locations",
        "Your journal tracks all your discoveries"
    )

    var currentTip by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000)
            currentTip = (currentTip + 1) % tips.size
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            tint = GildedGold.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = tips[currentTip],
            style = MaterialTheme.typography.bodySmall,
            color = AncientParchment.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Error screen when experience generation fails
 */
@Composable
fun ExperienceErrorScreen(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepLibraryBrown),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Error icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFE91E63).copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Something Went Wrong",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AncientParchment
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = AncientParchment.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AncientParchment
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Go Back")
                }

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GildedGold,
                        contentColor = DeepLibraryBrown
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Try Again")
                }
            }
        }
    }
}
