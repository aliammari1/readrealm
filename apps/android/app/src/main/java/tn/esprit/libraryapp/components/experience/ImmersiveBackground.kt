package tn.esprit.libraryapp.components.experience

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Theme colors
private val DeepLibraryBrown = Color(0xFF2C1810)
private val MysticPurple = Color(0xFF4A1942)

/**
 * Immersive background effects for the experience
 */
@Composable
fun ImmersiveBackground(
    worldType: String,
    timeOfDay: String,
    weatherType: String,
    particlesEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Sky gradient based on time of day
        SkyGradientLayer(timeOfDay = timeOfDay)

        // Weather effects
        if (particlesEnabled) {
            WeatherParticles(weatherType = weatherType)
        }

        // Ambient particles based on world type
        if (particlesEnabled) {
            AmbientParticles(worldType = worldType)
        }
    }
}

@Composable
private fun SkyGradientLayer(timeOfDay: String) {
    val colors = when (timeOfDay.uppercase()) {
        "DAWN" -> listOf(
            Color(0xFF1a1a2e),
            Color(0xFF4a3860),
            Color(0xFFe6a57a),
            Color(0xFFffcba4)
        )
        "MORNING" -> listOf(
            Color(0xFF87CEEB),
            Color(0xFFB4E7FF),
            Color(0xFFFFF8E7)
        )
        "NOON", "DAY" -> listOf(
            Color(0xFF4A90D9),
            Color(0xFF87CEEB),
            Color(0xFFF0F8FF)
        )
        "AFTERNOON" -> listOf(
            Color(0xFF6BA3D6),
            Color(0xFFADD8E6),
            Color(0xFFFFFAF0)
        )
        "DUSK", "TWILIGHT" -> listOf(
            Color(0xFF2C1810),
            Color(0xFF4A1942),
            Color(0xFFE65C00),
            Color(0xFFFFAA00)
        )
        "EVENING" -> listOf(
            Color(0xFF1a1a2e),
            Color(0xFF4a2060),
            Color(0xFF2d132c)
        )
        "NIGHT", "MIDNIGHT" -> listOf(
            Color(0xFF0a0a15),
            Color(0xFF1a1a2e),
            Color(0xFF16213e)
        )
        else -> listOf(
            DeepLibraryBrown,
            MysticPurple,
            Color(0xFF2C1810)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors))
    )
}

@Composable
private fun WeatherParticles(weatherType: String) {
    when (weatherType.uppercase()) {
        "RAIN" -> RainEffect()
        "SNOW" -> SnowEffect()
        "STORM" -> StormEffect()
        "FOG" -> FogEffect()
        else -> {} // Clear weather, no particles
    }
}

@Composable
private fun RainEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainOffset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val rainColor = Color(0xFF6BA3D6).copy(alpha = 0.4f)
        val dropCount = 100
        
        for (i in 0 until dropCount) {
            val x = (size.width / dropCount) * i + Random.nextFloat() * 10
            val startY = ((offset * size.height) + (i * 47) % size.height) % size.height
            val endY = startY + 30f
            
            drawLine(
                color = rainColor,
                start = Offset(x, startY),
                end = Offset(x - 3f, endY),
                strokeWidth = 1.5f
            )
        }
    }
}

@Composable
private fun SnowEffect() {
    val snowflakes = remember { List(80) { Snowflake() } }
    val infiniteTransition = rememberInfiniteTransition(label = "snow")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        snowflakes.forEach { flake ->
            val yPos = ((time * size.height * flake.speed) + flake.startY) % size.height
            val xPos = flake.startX * size.width + sin(time * 6.28f + flake.phase) * 20f
            
            drawCircle(
                color = Color.White.copy(alpha = flake.alpha),
                radius = flake.size,
                center = Offset(xPos, yPos)
            )
        }
    }
}

private data class Snowflake(
    val startX: Float = Random.nextFloat(),
    val startY: Float = Random.nextFloat() * 1000f,
    val size: Float = Random.nextFloat() * 4f + 2f,
    val speed: Float = Random.nextFloat() * 0.5f + 0.3f,
    val alpha: Float = Random.nextFloat() * 0.5f + 0.3f,
    val phase: Float = Random.nextFloat() * 6.28f
)

@Composable
private fun StormEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "storm")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                0f at 0
                0f at 2000
                0.8f at 2050
                0f at 2100
                0.4f at 2150
                0f at 2200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "flash"
    )

    // Rain layer
    RainEffect()

    // Lightning flash
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = flashAlpha))
    )
}

@Composable
private fun FogEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "fog")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fogDrift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Multiple fog layers
        for (layer in 0 until 3) {
            val layerOffset = drift * size.width * 0.3f + layer * 100f
            val alpha = 0.15f - layer * 0.03f
            
            drawCircle(
                color = Color.Gray.copy(alpha = alpha),
                radius = size.width * 0.6f,
                center = Offset(
                    x = (layerOffset % (size.width * 2)) - size.width * 0.5f,
                    y = size.height * 0.7f - layer * size.height * 0.1f
                )
            )
        }
    }
}

@Composable
private fun AmbientParticles(worldType: String) {
    when (worldType.uppercase()) {
        "FANTASY" -> MagicOrbsEffect()
        "MAGICAL_REALISM" -> MagicOrbsEffect()
        "HORROR" -> SpookyEffect()
        "SCIENCE_FICTION", "CYBERPUNK" -> TechParticlesEffect()
        "HISTORICAL", "VICTORIAN" -> DustParticlesEffect()
        else -> FirefliesEffect()
    }
}

@Composable
private fun MagicOrbsEffect() {
    val orbs = remember { List(20) { MagicOrb() } }
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        orbs.forEach { orb ->
            val x = orb.baseX * size.width + sin(time * orb.speed + orb.phase) * 50f
            val y = orb.baseY * size.height + cos(time * orb.speed * 0.7f + orb.phase) * 30f
            val pulseAlpha = (sin(time * 2 + orb.phase) * 0.3f + 0.5f).coerceIn(0.2f, 0.8f)
            
            // Glow
            drawCircle(
                color = orb.color.copy(alpha = pulseAlpha * 0.3f),
                radius = orb.size * 3,
                center = Offset(x, y)
            )
            // Core
            drawCircle(
                color = orb.color.copy(alpha = pulseAlpha),
                radius = orb.size,
                center = Offset(x, y)
            )
        }
    }
}

private data class MagicOrb(
    val baseX: Float = Random.nextFloat(),
    val baseY: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 8f + 4f,
    val speed: Float = Random.nextFloat() * 0.5f + 0.5f,
    val phase: Float = Random.nextFloat() * 6.28f,
    val color: Color = listOf(
        Color(0xFF9C27B0),
        Color(0xFF00BCD4),
        Color(0xFFD4AF37),
        Color(0xFF4CAF50)
    ).random()
)

@Composable
private fun FirefliesEffect() {
    val fireflies = remember { List(30) { Firefly() } }
    val infiniteTransition = rememberInfiniteTransition(label = "fireflies")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fireflyTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        fireflies.forEach { fly ->
            val blinkPhase = (sin(time * fly.blinkSpeed + fly.blinkOffset) + 1f) / 2f
            val alpha = blinkPhase * 0.8f
            val x = fly.baseX * size.width + sin(time + fly.phase) * 30f
            val y = fly.baseY * size.height + cos(time * 0.7f + fly.phase) * 20f
            
            if (alpha > 0.1f) {
                drawCircle(
                    color = Color(0xFFFFEB3B).copy(alpha = alpha * 0.3f),
                    radius = 12f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color(0xFFFFEB3B).copy(alpha = alpha),
                    radius = 4f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

private data class Firefly(
    val baseX: Float = Random.nextFloat(),
    val baseY: Float = Random.nextFloat(),
    val phase: Float = Random.nextFloat() * 6.28f,
    val blinkSpeed: Float = Random.nextFloat() * 3f + 2f,
    val blinkOffset: Float = Random.nextFloat() * 6.28f
)

@Composable
private fun SpookyEffect() {
    val particles = remember { List(15) { SpookyParticle() } }
    val infiniteTransition = rememberInfiniteTransition(label = "spooky")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spookyTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val yPos = ((1f - time) * size.height * 1.5f + particle.startY) % (size.height * 1.5f)
            val xPos = particle.startX * size.width + sin(time * 6.28f * particle.drift) * 50f
            val fadeAlpha = if (yPos < size.height * 0.2f) yPos / (size.height * 0.2f) else 1f
            
            drawCircle(
                color = Color(0xFF1a1a2e).copy(alpha = 0.4f * fadeAlpha * particle.alpha),
                radius = particle.size * 2,
                center = Offset(xPos, yPos)
            )
        }
    }
}

private data class SpookyParticle(
    val startX: Float = Random.nextFloat(),
    val startY: Float = Random.nextFloat() * 1000f,
    val size: Float = Random.nextFloat() * 20f + 10f,
    val drift: Float = Random.nextFloat() * 0.5f + 0.2f,
    val alpha: Float = Random.nextFloat() * 0.5f + 0.3f
)

@Composable
private fun TechParticlesEffect() {
    val particles = remember { List(40) { TechParticle() } }
    val infiniteTransition = rememberInfiniteTransition(label = "tech")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "techTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val progress = (time + particle.delay) % 1f
            val x = particle.startX * size.width
            val y = progress * size.height
            
            drawLine(
                color = particle.color.copy(alpha = 0.6f * (1f - progress)),
                start = Offset(x, y),
                end = Offset(x, y + 20f),
                strokeWidth = 2f
            )
        }
    }
}

private data class TechParticle(
    val startX: Float = Random.nextFloat(),
    val delay: Float = Random.nextFloat(),
    val color: Color = listOf(
        Color(0xFF00FFFF),
        Color(0xFF00FF00),
        Color(0xFFFF00FF)
    ).random()
)

@Composable
private fun DustParticlesEffect() {
    val particles = remember { List(50) { DustParticle() } }
    val infiniteTransition = rememberInfiniteTransition(label = "dust")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dustTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val x = ((time * size.width * particle.speed) + particle.startX * size.width) % size.width
            val y = particle.startY * size.height + sin(time * 6.28f + particle.phase) * 20f
            
            drawCircle(
                color = Color(0xFFF5E6D3).copy(alpha = particle.alpha),
                radius = particle.size,
                center = Offset(x, y)
            )
        }
    }
}

private data class DustParticle(
    val startX: Float = Random.nextFloat(),
    val startY: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 2f + 1f,
    val speed: Float = Random.nextFloat() * 0.3f + 0.1f,
    val alpha: Float = Random.nextFloat() * 0.3f + 0.1f,
    val phase: Float = Random.nextFloat() * 6.28f
)
