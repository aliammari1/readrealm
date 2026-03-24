package tn.esprit.libraryapp.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

@Composable
fun ParticleEffect() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val particles = remember {
        List(25) {
            FloatingParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 20f + 8f,
                speedX = (Random.nextFloat() - 0.5f) * 0.3f,
                speedY = Random.nextFloat() * 0.5f + 0.2f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 2f,
                type = ParticleType.values()[Random.nextInt(ParticleType.values().size)],
                alpha = Random.nextFloat() * 0.3f + 0.1f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEachIndexed { index, particle ->
            val phase = (time + index * 30) % 360
            val phaseRad = phase * PI.toFloat() / 180f

            val currentX = ((particle.x + sin(phaseRad) * particle.speedX + time / 360f * particle.speedX) % 1f) * canvasWidth
            val currentY = ((particle.y + time / 360f * particle.speedY) % 1f) * canvasHeight
            val currentRotation = particle.rotation + time * particle.rotationSpeed

            when (particle.type) {
                ParticleType.BOOK_PAGE -> {
                    drawBookPage(
                        center = Offset(currentX, currentY),
                        size = particle.size,
                        rotation = currentRotation,
                        alpha = particle.alpha,
                        color = secondaryColor
                    )
                }
                ParticleType.SPARKLE -> {
                    drawSparkle(
                        center = Offset(currentX, currentY),
                        size = particle.size * 0.5f,
                        rotation = currentRotation,
                        alpha = particle.alpha * (0.5f + 0.5f * sin(phaseRad)),
                        color = primaryColor
                    )
                }
                ParticleType.DUST -> {
                    drawCircle(
                        color = primaryColor.copy(alpha = particle.alpha * 0.5f),
                        radius = particle.size * 0.3f,
                        center = Offset(currentX, currentY)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawBookPage(
    center: Offset,
    size: Float,
    rotation: Float,
    alpha: Float,
    color: Color
) {
    rotate(degrees = rotation, pivot = center) {
        val pageWidth = size * 1.5f
        val pageHeight = size * 2f

        // Page shadow
        drawRoundRect(
            color = Color.Black.copy(alpha = alpha * 0.2f),
            topLeft = Offset(center.x - pageWidth / 2 + 2.dp.toPx(), center.y - pageHeight / 2 + 2.dp.toPx()),
            size = Size(pageWidth, pageHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )

        // Page background
        drawRoundRect(
            color = Color(0xFFFFF8DC).copy(alpha = alpha),
            topLeft = Offset(center.x - pageWidth / 2, center.y - pageHeight / 2),
            size = Size(pageWidth, pageHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )

        // Page lines (text simulation)
        val lineCount = 4
        for (i in 0 until lineCount) {
            val lineY = center.y - pageHeight / 2 + (i + 1) * (pageHeight / (lineCount + 1))
            val lineWidth = pageWidth * (0.6f + Random.nextFloat() * 0.3f)
            drawLine(
                color = color.copy(alpha = alpha * 0.3f),
                start = Offset(center.x - lineWidth / 2, lineY),
                end = Offset(center.x + lineWidth / 2, lineY),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

private fun DrawScope.drawSparkle(
    center: Offset,
    size: Float,
    rotation: Float,
    alpha: Float,
    color: Color
) {
    rotate(degrees = rotation, pivot = center) {
        val armLength = size
        val armWidth = size * 0.15f

        // Four-pointed star
        for (angle in listOf(0f, 90f)) {
            rotate(degrees = angle, pivot = center) {
                drawLine(
                    color = color.copy(alpha = alpha),
                    start = Offset(center.x, center.y - armLength),
                    end = Offset(center.x, center.y + armLength),
                    strokeWidth = armWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        // Center glow
        drawCircle(
            color = Color.White.copy(alpha = alpha * 0.8f),
            radius = size * 0.3f,
            center = center
        )
    }
}

data class FloatingParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speedX: Float,
    val speedY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val type: ParticleType,
    val alpha: Float
)

enum class ParticleType {
    BOOK_PAGE, SPARKLE, DUST
}

data class Particle(val position: Offset, val velocity: Float) {
    fun update(size: androidx.compose.ui.geometry.Size): Particle {
        val newY = (position.y + velocity) % size.height
        return copy(position = Offset(position.x, newY))
    }
}
