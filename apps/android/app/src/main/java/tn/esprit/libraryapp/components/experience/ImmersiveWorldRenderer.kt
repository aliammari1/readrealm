package tn.esprit.libraryapp.components.experience

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tn.esprit.libraryapp.models.experience.*
import tn.esprit.libraryapp.ui.theme.*
import kotlin.math.*
import kotlin.random.Random

/**
 * ═══════════════════════════════════════════════════════════════════
 * IMMERSIVE WORLD RENDERER
 * 3D-like world visualization with parallax and depth effects
 * ═══════════════════════════════════════════════════════════════════
 */

// Helper function to get colors based on WorldAtmosphere
private fun getAtmosphereColors(atmosphere: WorldAtmosphere): Pair<Color, Color> {
    return when (atmosphere) {
        WorldAtmosphere.BRIGHT -> Color(0xFF87CEEB) to Color(0xFFFFFAF0)
        WorldAtmosphere.DARK -> Color(0xFF1A1A2E) to Color(0xFF16213E)
        WorldAtmosphere.MYSTICAL -> Color(0xFF4A1942) to Color(0xFF2C1810)
        WorldAtmosphere.GLOOMY -> Color(0xFF2F4F4F) to Color(0xFF1C1C1C)
        WorldAtmosphere.SERENE -> Color(0xFF98D8C8) to Color(0xFFE8F5E9)
        WorldAtmosphere.CHAOTIC -> Color(0xFF8B0000) to Color(0xFF2F0000)
        WorldAtmosphere.ETHEREAL -> Color(0xFFE6E6FA) to Color(0xFFB8A9C9)
        WorldAtmosphere.GRITTY -> Color(0xFF4A4A4A) to Color(0xFF2A2A2A)
        WorldAtmosphere.WHIMSICAL -> Color(0xFFFFB6C1) to Color(0xFFE6E6FA)
        WorldAtmosphere.OPPRESSIVE -> Color(0xFF1A1A1A) to Color(0xFF0D0D0D)
        WorldAtmosphere.LIBERATING -> Color(0xFF87CEEB) to Color(0xFFFFFFFF)
    }
}

@Composable
fun ImmersiveWorldRenderer(
    world: LiteraryWorld,
    currentLocation: WorldLocation,
    ambiance: WorldAmbiance,
    onLocationTap: (WorldLocation) -> Unit,
    onPoiTap: (PointOfInterest) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "world")
    
    // Atmospheric movement
    val atmosphereShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "atmosphere"
    )
    
    // Camera shake for dramatic moments
    var cameraOffset by remember { mutableStateOf(Offset.Zero) }
    
    // Get atmosphere colors
    val (primaryAtmosphereColor, secondaryAtmosphereColor) = getAtmosphereColors(world.atmosphere)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(primaryAtmosphereColor)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    cameraOffset = Offset(
                        x = (cameraOffset.x + dragAmount.x * 0.5f).coerceIn(-200f, 200f),
                        y = (cameraOffset.y + dragAmount.y * 0.5f).coerceIn(-200f, 200f)
                    )
                }
            }
    ) {
        // Layer 1: Sky/Background
        SkyLayer(
            world = world,
            atmosphereShift = atmosphereShift,
            cameraOffset = cameraOffset
        )
        
        // Layer 2: Distant elements (mountains, buildings)
        DistantLayer(
            world = world,
            atmosphereShift = atmosphereShift,
            cameraOffset = cameraOffset,
            ambiance = ambiance
        )
        
        // Layer 3: Mid-ground (trees, structures)
        MidgroundLayer(
            location = currentLocation,
            atmosphereShift = atmosphereShift,
            cameraOffset = cameraOffset
        )
        
        // Layer 4: Foreground with POIs
        ForegroundLayer(
            location = currentLocation,
            world = world,
            onPoiTap = onPoiTap,
            onLocationTap = onLocationTap,
            cameraOffset = cameraOffset
        )
        
        // Layer 5: Weather & Particle Effects
        WeatherEffectsLayer(
            world = world,
            atmosphereShift = atmosphereShift
        )
        
        // Layer 6: Magical/Special Effects
        MagicalEffectsLayer(
            world = world,
            location = currentLocation,
            ambiance = ambiance
        )
        
        // Vignette overlay for depth
        VignetteOverlay()
    }
}

@Composable
private fun SkyLayer(
    world: LiteraryWorld,
    atmosphereShift: Float,
    cameraOffset: Offset
) {
    val skyColors = when (world.timeOfDay) {
        TimeOfDay.DAWN -> listOf(
            Color(0xFF1A0533),
            Color(0xFF4A1942),
            Color(0xFFFF6B35),
            Color(0xFFFFD93D)
        )
        TimeOfDay.MORNING -> listOf(
            Color(0xFF87CEEB),
            Color(0xFFB0E0E6),
            Color(0xFFFFFAF0)
        )
        TimeOfDay.NOON -> listOf(
            Color(0xFF4169E1),
            Color(0xFF87CEEB),
            Color(0xFFADD8E6)
        )
        TimeOfDay.DUSK, TimeOfDay.EVENING -> listOf(
            Color(0xFF2C1654),
            Color(0xFF6B2D5C),
            Color(0xFFD4556B),
            Color(0xFFFFB347)
        )
        TimeOfDay.NIGHT, TimeOfDay.MIDNIGHT -> listOf(
            Color(0xFF0D0D1A),
            Color(0xFF1A1A2E),
            Color(0xFF16213E)
        )
        else -> {
            val (primary, secondary) = getAtmosphereColors(world.atmosphere)
            listOf(primary, secondary)
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Sky gradient
        drawRect(
            brush = Brush.verticalGradient(skyColors),
            size = size
        )
        
        // Stars for night scenes
        if (world.timeOfDay == TimeOfDay.NIGHT || world.timeOfDay == TimeOfDay.MIDNIGHT) {
            val starCount = 100
            repeat(starCount) { i ->
                val x = (size.width * ((i * 17 + atmosphereShift * 50) % 100) / 100)
                val y = (size.height * 0.6f * ((i * 31) % 100) / 100)
                val starSize = 1f + (i % 3)
                val twinkle = sin((atmosphereShift + i * 0.1f) * PI * 2).toFloat() * 0.5f + 0.5f
                
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f + twinkle * 0.7f),
                    radius = starSize,
                    center = Offset(x, y)
                )
            }
        }
        
        // Sun or Moon
        val celestialX = size.width * (0.7f + cameraOffset.x * 0.0005f)
        val celestialY = size.height * 0.15f + cameraOffset.y * 0.002f
        
        if (world.timeOfDay == TimeOfDay.NIGHT || world.timeOfDay == TimeOfDay.MIDNIGHT) {
            // Moon
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFF5F5DC), Color(0xFFE8E8D0)),
                    center = Offset(celestialX, celestialY)
                ),
                radius = 40f,
                center = Offset(celestialX, celestialY)
            )
            // Moon glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x40F5F5DC),
                        Color.Transparent
                    ),
                    center = Offset(celestialX, celestialY),
                    radius = 100f
                ),
                radius = 100f,
                center = Offset(celestialX, celestialY)
            )
        } else if (world.timeOfDay != TimeOfDay.NIGHT) {
            // Sun
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00)),
                    center = Offset(celestialX, celestialY)
                ),
                radius = 35f,
                center = Offset(celestialX, celestialY)
            )
            // Sun rays
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x60FFD700),
                        Color.Transparent
                    ),
                    center = Offset(celestialX, celestialY),
                    radius = 120f
                ),
                radius = 120f,
                center = Offset(celestialX, celestialY)
            )
        }
    }
}

@Composable
private fun DistantLayer(
    world: LiteraryWorld,
    atmosphereShift: Float,
    cameraOffset: Offset,
    ambiance: WorldAmbiance
) {
    val parallaxFactor = 0.1f
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val offsetX = cameraOffset.x * parallaxFactor
        val offsetY = cameraOffset.y * parallaxFactor
        
        when (world.type) {
            WorldType.FANTASY -> {
                // Distant mountains with castles
                drawMountainRange(
                    startY = size.height * 0.4f + offsetY,
                    color = Color(0xFF2D1B4E).copy(alpha = 0.6f),
                    peaks = 5,
                    offsetX = offsetX
                )
                drawMountainRange(
                    startY = size.height * 0.5f + offsetY,
                    color = Color(0xFF3D2B5E).copy(alpha = 0.7f),
                    peaks = 4,
                    offsetX = offsetX + 50
                )
            }
            WorldType.SCIENCE_FICTION -> {
                // Distant cityscape
                drawFuturisticSkyline(
                    startY = size.height * 0.4f + offsetY,
                    offsetX = offsetX,
                    atmosphereShift = atmosphereShift
                )
            }
            WorldType.HISTORICAL, WorldType.CONTEMPORARY -> {
                // Rolling hills or city buildings
                drawHills(
                    startY = size.height * 0.5f + offsetY,
                    color = Color(0xFF4A6741).copy(alpha = 0.5f),
                    offsetX = offsetX
                )
            }
            else -> {
                drawMountainRange(
                    startY = size.height * 0.45f + offsetY,
                    color = Color(ambiance.secondaryColor).copy(alpha = 0.5f),
                    peaks = 6,
                    offsetX = offsetX
                )
            }
        }
    }
}

private fun DrawScope.drawMountainRange(
    startY: Float,
    color: Color,
    peaks: Int,
    offsetX: Float
) {
    val path = Path().apply {
        moveTo(-50f + offsetX, size.height)
        
        val segmentWidth = (size.width + 100) / peaks
        repeat(peaks) { i ->
            val peakX = i * segmentWidth + segmentWidth / 2 + offsetX
            val peakY = startY - Random(i).nextFloat() * 150 - 50
            val controlY = startY + 20
            
            quadraticTo(
                i * segmentWidth + offsetX, controlY,
                peakX, peakY
            )
            quadraticTo(
                (i + 1) * segmentWidth + offsetX, controlY,
                (i + 1) * segmentWidth + offsetX, startY
            )
        }
        
        lineTo(size.width + 50, size.height)
        close()
    }
    
    drawPath(path, color)
}

private fun DrawScope.drawFuturisticSkyline(
    startY: Float,
    offsetX: Float,
    atmosphereShift: Float
) {
    val buildingCount = 15
    val buildingWidth = size.width / buildingCount
    
    repeat(buildingCount) { i ->
        val height = 50f + Random(i).nextFloat() * 200
        val x = i * buildingWidth + offsetX * 0.5f
        
        // Building body
        drawRect(
            color = Color(0xFF1A1A2E),
            topLeft = Offset(x, startY - height),
            size = Size(buildingWidth - 5, height + size.height)
        )
        
        // Windows with glow
        val windowRows = (height / 20).toInt()
        repeat(windowRows) { row ->
            repeat(3) { col ->
                val lit = Random(i * 100 + row * 10 + col).nextFloat() > 0.3f
                if (lit) {
                    val windowX = x + 5 + col * (buildingWidth / 4)
                    val windowY = startY - height + 10 + row * 20
                    
                    drawRect(
                        color = Color(0xFFFFD700).copy(
                            alpha = 0.5f + sin((atmosphereShift + i * 0.1f) * PI).toFloat() * 0.3f
                        ),
                        topLeft = Offset(windowX, windowY),
                        size = Size(8f, 12f)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawHills(
    startY: Float,
    color: Color,
    offsetX: Float
) {
    val path = Path().apply {
        moveTo(-50f + offsetX, size.height)
        
        var x = -50f + offsetX
        while (x < size.width + 50) {
            val hillWidth = 150f + Random((x / 100).toInt()).nextFloat() * 100
            val hillHeight = 30f + Random((x / 50).toInt()).nextFloat() * 60
            
            quadraticTo(
                x + hillWidth / 2, startY - hillHeight,
                x + hillWidth, startY
            )
            x += hillWidth
        }
        
        lineTo(size.width + 50, size.height)
        close()
    }
    
    drawPath(path, color)
}

@Composable
private fun MidgroundLayer(
    location: WorldLocation,
    atmosphereShift: Float,
    cameraOffset: Offset
) {
    val parallaxFactor = 0.3f
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val offsetX = cameraOffset.x * parallaxFactor
        val offsetY = cameraOffset.y * parallaxFactor
        
        when (location.type) {
            LocationType.FOREST -> {
                drawForest(
                    startY = size.height * 0.55f + offsetY,
                    offsetX = offsetX,
                    atmosphereShift = atmosphereShift
                )
            }
            LocationType.CASTLE, LocationType.PALACE -> {
                drawCastle(
                    centerX = size.width / 2 + offsetX,
                    baseY = size.height * 0.7f + offsetY
                )
            }
            LocationType.CITY, LocationType.VILLAGE -> {
                drawBuildings(
                    startY = size.height * 0.6f + offsetY,
                    offsetX = offsetX
                )
            }
            LocationType.OCEAN, LocationType.SHIP -> {
                drawOcean(
                    startY = size.height * 0.6f + offsetY,
                    atmosphereShift = atmosphereShift
                )
            }
            else -> {
                // Generic terrain
                drawTerrain(
                    startY = size.height * 0.6f + offsetY,
                    offsetX = offsetX
                )
            }
        }
    }
}

private fun DrawScope.drawForest(
    startY: Float,
    offsetX: Float,
    atmosphereShift: Float
) {
    val treeCount = 20
    
    repeat(treeCount) { i ->
        val x = (size.width * i / treeCount) + offsetX + sin(i.toFloat()) * 30
        val treeHeight = 80f + Random(i).nextFloat() * 120
        val swayOffset = sin((atmosphereShift * PI * 2 + i * 0.5).toFloat()) * 3
        
        // Tree trunk
        drawRect(
            color = Color(0xFF4A3728),
            topLeft = Offset(x - 8 + swayOffset, startY - treeHeight * 0.3f),
            size = Size(16f, treeHeight * 0.4f)
        )
        
        // Tree foliage (triangle)
        val foliagePath = Path().apply {
            moveTo(x + swayOffset, startY - treeHeight)
            lineTo(x - 40 + swayOffset * 0.5f, startY - treeHeight * 0.2f)
            lineTo(x + 40 + swayOffset * 0.5f, startY - treeHeight * 0.2f)
            close()
        }
        
        drawPath(
            foliagePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1B4332),
                    Color(0xFF2D6A4F)
                )
            )
        )
    }
}

private fun DrawScope.drawCastle(centerX: Float, baseY: Float) {
    val castleWidth = 300f
    val castleHeight = 250f
    
    // Main building
    drawRect(
        color = Color(0xFF4A4A5A),
        topLeft = Offset(centerX - castleWidth / 2, baseY - castleHeight),
        size = Size(castleWidth, castleHeight)
    )
    
    // Towers
    listOf(-castleWidth / 2 - 20, castleWidth / 2 - 20).forEach { towerOffset ->
        val towerX = centerX + towerOffset
        
        // Tower body
        drawRect(
            color = Color(0xFF3A3A4A),
            topLeft = Offset(towerX, baseY - castleHeight - 80),
            size = Size(60f, castleHeight + 80)
        )
        
        // Tower roof (cone)
        val roofPath = Path().apply {
            moveTo(towerX + 30, baseY - castleHeight - 150)
            lineTo(towerX - 10, baseY - castleHeight - 80)
            lineTo(towerX + 70, baseY - castleHeight - 80)
            close()
        }
        drawPath(roofPath, Color(0xFF8B0000))
    }
    
    // Windows
    repeat(3) { row ->
        repeat(4) { col ->
            drawRect(
                color = Color(0xFFFFD700).copy(alpha = 0.7f),
                topLeft = Offset(
                    centerX - castleWidth / 2 + 30 + col * 70,
                    baseY - castleHeight + 40 + row * 70
                ),
                size = Size(25f, 40f)
            )
        }
    }
    
    // Gate
    drawArc(
        color = Color(0xFF2A2A3A),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(centerX - 40, baseY - 100),
        size = Size(80f, 100f)
    )
}

private fun DrawScope.drawBuildings(startY: Float, offsetX: Float) {
    val buildingCount = 8
    val spacing = size.width / buildingCount
    
    repeat(buildingCount) { i ->
        val x = i * spacing + offsetX
        val height = 60f + Random(i * 7).nextFloat() * 100
        val width = spacing * 0.8f
        
        // Building
        drawRect(
            color = Color(0xFF8B7355).copy(alpha = 0.9f),
            topLeft = Offset(x, startY - height),
            size = Size(width, height + size.height)
        )
        
        // Roof
        val roofPath = Path().apply {
            moveTo(x - 10, startY - height)
            lineTo(x + width / 2, startY - height - 30)
            lineTo(x + width + 10, startY - height)
            close()
        }
        drawPath(roofPath, Color(0xFF654321))
        
        // Windows
        repeat(2) { row ->
            repeat(2) { col ->
                drawRect(
                    color = Color(0xFFFFE4B5).copy(alpha = 0.8f),
                    topLeft = Offset(x + 10 + col * (width / 2 - 10), startY - height + 20 + row * 35),
                    size = Size(width / 4, 25f)
                )
            }
        }
    }
}

private fun DrawScope.drawOcean(startY: Float, atmosphereShift: Float) {
    // Ocean body
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF006994),
                Color(0xFF003366),
                Color(0xFF001833)
            ),
            startY = startY,
            endY = size.height
        ),
        topLeft = Offset(0f, startY),
        size = Size(size.width, size.height - startY)
    )
    
    // Waves
    repeat(5) { waveIndex ->
        val waveY = startY + waveIndex * 30
        val waveOffset = atmosphereShift * size.width + waveIndex * 50
        
        val wavePath = Path().apply {
            moveTo(-50f + waveOffset % 100, waveY + 15)
            var x = -50f + waveOffset % 100
            while (x < size.width + 150) {
                val waveHeight = 8f + waveIndex * 2
                quadraticTo(
                    x + 25, waveY - waveHeight,
                    x + 50, waveY
                )
                quadraticTo(
                    x + 75, waveY + waveHeight,
                    x + 100, waveY
                )
                x += 100
            }
            lineTo(size.width + 50, size.height)
            lineTo(-50f, size.height)
            close()
        }
        
        drawPath(
            wavePath,
            Color(0xFF4169E1).copy(alpha = 0.3f - waveIndex * 0.05f)
        )
    }
}

private fun DrawScope.drawTerrain(startY: Float, offsetX: Float) {
    // Ground
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF5D4E37),
                Color(0xFF3D2E17)
            )
        ),
        topLeft = Offset(0f, startY),
        size = Size(size.width, size.height - startY)
    )
    
    // Grass patches
    repeat(30) { i ->
        val x = (size.width * i / 30) + offsetX + Random(i).nextFloat() * 20
        val grassHeight = 15f + Random(i * 3).nextFloat() * 20
        
        repeat(5) { blade ->
            val bladeX = x + blade * 4
            val sway = sin(blade.toFloat()) * 3
            
            drawLine(
                color = Color(0xFF228B22).copy(alpha = 0.8f),
                start = Offset(bladeX, startY),
                end = Offset(bladeX + sway, startY - grassHeight),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
private fun ForegroundLayer(
    location: WorldLocation,
    world: LiteraryWorld,
    onPoiTap: (PointOfInterest) -> Unit,
    onLocationTap: (WorldLocation) -> Unit,
    cameraOffset: Offset
) {
    val parallaxFactor = 0.5f
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Interactive POIs
        location.pointsOfInterest.forEach { poi ->
            val adjustedX = poi.position.x + cameraOffset.x * parallaxFactor
            val adjustedY = poi.position.y + cameraOffset.y * parallaxFactor
            
            InteractivePoiMarker(
                poi = poi,
                x = adjustedX,
                y = adjustedY,
                onTap = { onPoiTap(poi) }
            )
        }
        
        // Connected location navigation markers
        location.connectedLocations.forEachIndexed { index, connectedId ->
            val connectedLocation = world.locations.find { it.id == connectedId }
            connectedLocation?.let { targetLocation ->
                LocationNavigationMarker(
                    targetLocation = targetLocation,
                    position = getNavigationPosition(index, location.connectedLocations.size),
                    cameraOffset = cameraOffset,
                    parallaxFactor = parallaxFactor,
                    onTap = { onLocationTap(targetLocation) }
                )
            }
        }
    }
}

// Helper function to position navigation markers around the edges
private fun getNavigationPosition(index: Int, total: Int): Offset {
    val angle = (index.toFloat() / total) * 2 * PI + PI / 4
    val radius = 350f
    return Offset(
        x = 500f + cos(angle).toFloat() * radius,
        y = 500f + sin(angle).toFloat() * radius
    )
}

@Composable
private fun LocationNavigationMarker(
    targetLocation: WorldLocation,
    position: Offset,
    cameraOffset: Offset,
    parallaxFactor: Float,
    onTap: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "navMarker")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val arrowBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    val density = LocalDensity.current
    val adjustedX = position.x + cameraOffset.x * parallaxFactor
    val adjustedY = position.y + cameraOffset.y * parallaxFactor
    
    Box(
        modifier = Modifier
            .offset(
                x = with(density) { adjustedX.toDp() },
                y = with(density) { (adjustedY + arrowBounce).toDp() }
            )
            .scale(pulseScale)
            .pointerInput(Unit) {
                detectTapGestures { onTap() }
            },
        contentAlignment = Alignment.Center
    ) {
        // Navigation portal effect
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GildedGold.copy(alpha = 0.6f),
                            MysticPurple.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Inner portal
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            GildedGold.copy(alpha = 0.7f),
                            MysticPurple.copy(alpha = 0.5f)
                        )
                    ),
                    shape = CircleShape
                )
                .border(2.dp, GildedGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "Go to ${targetLocation.name}",
                tint = DeepLibraryBrown,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Location name label
        Text(
            text = targetLocation.name,
            color = AncientParchment,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .offset(y = 50.dp)
                .background(
                    color = DeepLibraryBrown.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun InteractivePoiMarker(
    poi: PointOfInterest,
    x: Float,
    y: Float,
    onTap: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "poi")
    
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    val density = LocalDensity.current
    
    Box(
        modifier = Modifier
            .offset(
                x = with(density) { x.toDp() },
                y = with(density) { (y + floatOffset).toDp() }
            )
            .size(60.dp)
            .pointerInput(Unit) {
                detectTapGestures { onTap() }
            },
        contentAlignment = Alignment.Center
    ) {
        // Glow effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (poi.isDiscovered) GildedGold.copy(alpha = 0.4f * glowPulse)
                        else MysticPurple.copy(alpha = 0.4f * glowPulse),
                        Color.Transparent
                    )
                ),
                radius = size.width / 2
            )
        }
        
        // Icon based on POI type
        val icon = when (poi.type) {
            PoiType.ARTIFACT -> "🏺"
            PoiType.MONUMENT -> "🗿"
            PoiType.HIDDEN_PASSAGE -> "🚪"
            PoiType.TREASURE -> "💎"
            PoiType.JOURNAL_ENTRY -> "📖"
            PoiType.CHARACTER_MEMORY -> "💭"
            PoiType.PLOT_CLUE -> "🔍"
            PoiType.SCENIC_VIEW -> "🌄"
            PoiType.INTERACTIVE_OBJECT -> "✨"
            PoiType.PORTAL -> "🌀"
            PoiType.SECRET_ROOM -> "🗝️"
        }
        
        Text(
            text = icon,
            fontSize = 28.sp
        )
        
        // Undiscovered indicator
        if (!poi.isDiscovered) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(12.dp)
                    .background(MysticPurple, CircleShape)
            )
        }
    }
}

@Composable
private fun WeatherEffectsLayer(
    world: LiteraryWorld,
    atmosphereShift: Float
) {
    world.weatherPatterns.firstOrNull()?.let { weather ->
        when (weather.type) {
            WeatherType.RAIN -> RainEffect(intensity = weather.intensity)
            WeatherType.SNOW -> SnowEffect(intensity = weather.intensity)
            WeatherType.FOG -> FogEffect(density = weather.intensity)
            WeatherType.STORM -> StormEffect(intensity = weather.intensity, atmosphereShift = atmosphereShift)
            else -> {}
        }
    }
}

@Composable
private fun RainEffect(intensity: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    
    val rainOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((500 / intensity).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainfall"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val dropCount = (100 * intensity).toInt()
        
        repeat(dropCount) { i ->
            val x = (size.width * ((i * 17) % 100) / 100)
            val baseY = (size.height * ((i * 31 + rainOffset * 100) % 120) / 100) - size.height * 0.2f
            val dropLength = 15f + (i % 10)
            
            drawLine(
                color = Color(0x60A0C4E8),
                start = Offset(x, baseY),
                end = Offset(x - 3, baseY + dropLength),
                strokeWidth = 1.5f
            )
        }
    }
}

@Composable
private fun SnowEffect(intensity: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "snow")
    
    val snowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowfall"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val flakeCount = (80 * intensity).toInt()
        
        repeat(flakeCount) { i ->
            val x = (size.width * ((i * 23 + snowOffset * 50) % 100) / 100)
            val y = (size.height * ((i * 37 + snowOffset * 100) % 120) / 100) - size.height * 0.2f
            val flakeSize = 2f + (i % 4)
            val wobble = sin((snowOffset * PI * 4 + i).toFloat()) * 10
            
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = flakeSize,
                center = Offset(x + wobble, y)
            )
        }
    }
}

@Composable
private fun FogEffect(density: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Multiple fog layers
        repeat(3) { layer ->
            val alpha = 0.1f + density * 0.15f - layer * 0.03f
            val yOffset = size.height * (0.4f + layer * 0.15f)
            
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = alpha),
                        Color.White.copy(alpha = alpha * 0.7f),
                        Color.Transparent
                    ),
                    startY = yOffset,
                    endY = size.height
                )
            )
        }
    }
}

@Composable
private fun StormEffect(intensity: Float, atmosphereShift: Float) {
    var showLightning by remember { mutableStateOf(false) }
    
    LaunchedEffect(atmosphereShift) {
        if (Random.nextFloat() < 0.02f * intensity) {
            showLightning = true
            delay(100)
            showLightning = false
        }
    }
    
    // Rain
    RainEffect(intensity = intensity * 1.5f)
    
    // Lightning flash
    if (showLightning) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = Color.White.copy(alpha = 0.3f)
            )
            
            // Lightning bolt
            val boltPath = Path().apply {
                val startX = size.width * Random.nextFloat()
                moveTo(startX, 0f)
                
                var currentY = 0f
                while (currentY < size.height * 0.7f) {
                    val nextY = currentY + 30 + Random.nextFloat() * 50
                    val offsetX = (Random.nextFloat() - 0.5f) * 60
                    lineTo(startX + offsetX, nextY)
                    currentY = nextY
                }
            }
            
            drawPath(
                boltPath,
                color = Color(0xFFE0FFFF),
                style = Stroke(width = 3f)
            )
        }
    }
}

@Composable
private fun MagicalEffectsLayer(
    world: LiteraryWorld,
    location: WorldLocation,
    ambiance: WorldAmbiance
) {
    if (world.magicLevel > 0.3f) {
        MagicalParticles(density = world.magicLevel)
    }
    
    // Location-specific lighting effects
    when (location.lightingCondition) {
        LightingCondition.CANDLELIT, LightingCondition.FIRELIGHT -> {
            FlickeringLightEffect(intensity = 0.5f)
        }
        LightingCondition.BIOLUMINESCENT -> {
            BioluminescentGlowEffect(density = 0.7f)
        }
        LightingCondition.MAGICAL_GLOW -> {
            MagicOrbsEffect(density = 0.4f, color = MysticPurple)
        }
        else -> {}
    }
    
    ambiance.particleEffects.forEach { effect ->
        when (effect.type) {
            ParticleType.FIREFLIES -> FirefliesEffect(density = effect.density)
            ParticleType.MAGIC_ORBS -> MagicOrbsEffect(density = effect.density, color = Color(effect.color))
            ParticleType.EMBERS -> EmbersEffect(density = effect.density)
            ParticleType.DUST -> DustMotes(density = effect.density)
            else -> {}
        }
    }
}

@Composable
private fun MagicalParticles(density: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "magic")
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "magicTime"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val particleCount = (30 * density).toInt()
        
        repeat(particleCount) { i ->
            val angle = (time * 360 + i * (360f / particleCount)) * PI / 180
            val radius = 50f + sin((time * PI * 2 + i).toFloat()) * 30
            val x = size.width / 2 + cos(angle).toFloat() * radius * (i % 5 + 1)
            val y = size.height / 2 + sin(angle).toFloat() * radius * (i % 4 + 1)
            
            if (x > 0 && x < size.width && y > 0 && y < size.height) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE6E6FA),
                            Color(0xFF9370DB).copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        center = Offset(x, y),
                        radius = 8f
                    ),
                    radius = 8f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
private fun FirefliesEffect(density: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "fireflies")
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fireflyTime"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val fireflyCount = (20 * density).toInt()
        
        repeat(fireflyCount) { i ->
            val baseX = size.width * ((i * 37) % 100) / 100
            val baseY = size.height * 0.5f + size.height * 0.4f * ((i * 53) % 100) / 100
            
            val wobbleX = sin((time * PI * 3 + i * 0.7).toFloat()) * 30
            val wobbleY = cos((time * PI * 2 + i * 0.5).toFloat()) * 20
            val glow = (sin((time * PI * 4 + i).toFloat()) + 1) / 2
            
            if (glow > 0.3f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFFF00).copy(alpha = glow * 0.8f),
                            Color(0xFF90EE90).copy(alpha = glow * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(baseX + wobbleX, baseY + wobbleY),
                        radius = 12f
                    ),
                    radius = 12f,
                    center = Offset(baseX + wobbleX, baseY + wobbleY)
                )
            }
        }
    }
}

@Composable
private fun MagicOrbsEffect(density: Float, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbTime"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val orbCount = (10 * density).toInt()
        
        repeat(orbCount) { i ->
            val x = size.width * ((i * 29) % 100) / 100
            val baseY = size.height + 50
            val floatY = baseY - (time * size.height * 1.2f + i * (size.height / orbCount)) % (size.height * 1.2f)
            val wobbleX = sin((time * PI * 2 + i).toFloat()) * 20
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.8f),
                        color.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(x + wobbleX, floatY),
                    radius = 15f
                ),
                radius = 15f,
                center = Offset(x + wobbleX, floatY)
            )
        }
    }
}

@Composable
private fun EmbersEffect(density: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "embers")
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "emberTime"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val emberCount = (25 * density).toInt()
        
        repeat(emberCount) { i ->
            val x = size.width * ((i * 41) % 100) / 100
            val baseY = size.height
            val floatY = baseY - (time * size.height + i * 20) % size.height
            val wobbleX = sin((time * PI * 3 + i * 0.8).toFloat()) * 15
            val alpha = 1f - (floatY / size.height)
            
            drawCircle(
                color = Color(0xFFFF4500).copy(alpha = alpha * 0.8f),
                radius = 2f + (i % 3),
                center = Offset(x + wobbleX, floatY)
            )
        }
    }
}

@Composable
private fun FlickeringLightEffect(intensity: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "flicker")
    
    val flickerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0.1f at 0
                0.25f at 200
                0.15f at 400
                0.3f at 600
                0.2f at 800
                0.28f at 1000
                0.12f at 1200
                0.22f at 1400
                0.18f at 1600
                0.25f at 1800
                0.1f at 2000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "flickerAlpha"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Warm flickering overlay
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF8C00).copy(alpha = flickerAlpha * intensity),
                    Color(0xFFFF6600).copy(alpha = flickerAlpha * intensity * 0.5f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.6f),
                radius = size.width * 0.6f
            )
        )
    }
}

@Composable
private fun BioluminescentGlowEffect(density: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "biolum")
    
    val pulseTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val glowCount = (15 * density).toInt()
        
        repeat(glowCount) { i ->
            val x = size.width * ((i * 37) % 100) / 100
            val y = size.height * ((i * 53) % 100) / 100
            val phase = (pulseTime + i * 0.1f) % 1f
            val alpha = sin(phase * PI).toFloat() * 0.6f
            
            val glowColor = when (i % 3) {
                0 -> Color(0xFF00FFFF) // Cyan
                1 -> Color(0xFF7FFFD4) // Aquamarine
                else -> Color(0xFF00FF7F) // Spring green
            }
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = alpha),
                        glowColor.copy(alpha = alpha * 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(x, y),
                    radius = 40f + i % 20
                ),
                radius = 40f + i % 20,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun DustMotes(density: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "dust")
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dustTime"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val moteCount = (40 * density).toInt()
        
        repeat(moteCount) { i ->
            val x = (size.width * ((i * 31 + time * 100) % 100) / 100)
            val y = size.height * ((i * 47) % 100) / 100
            val shimmer = (sin((time * PI * 2 + i).toFloat()) + 1) / 2
            
            drawCircle(
                color = Color(0xFFFFE4B5).copy(alpha = 0.2f + shimmer * 0.3f),
                radius = 1.5f + (i % 2),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun VignetteOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.3f),
                    Color.Black.copy(alpha = 0.6f)
                ),
                center = Offset(size.width / 2, size.height / 2),
                radius = size.width * 0.8f
            )
        )
    }
}
