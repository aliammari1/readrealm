package tn.esprit.libraryapp.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun ParticleEffect() {
    val particles = remember {
        List(30) {
            mutableStateOf(
                Particle(
                    position =
                    Offset(Random.nextFloat() * 1000f, Random.nextFloat() * 2000f),
                    velocity = Random.nextFloat() * 2f + 1f
                )
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawCircle(
                color = Color.White.copy(0.2f),
                radius = 4.dp.toPx(),
                center = particle.value.position
            )
            particle.value = particle.value.update(size)
        }
    }
}

data class Particle(val position: Offset, val velocity: Float) {
    fun update(size: androidx.compose.ui.geometry.Size): Particle {
        val newY = (position.y + velocity) % size.height
        return copy(position = Offset(position.x, newY))
    }
}