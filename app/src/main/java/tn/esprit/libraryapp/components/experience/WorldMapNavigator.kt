package tn.esprit.libraryapp.components.experience

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
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
 * WORLD MAP NAVIGATOR
 * Interactive map showing all locations in the literary world
 * ═══════════════════════════════════════════════════════════════════
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapNavigator(
    world: LiteraryWorld,
    currentLocation: WorldLocation,
    visitedLocations: Set<String>,
    onLocationSelected: (WorldLocation) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLocation by remember { mutableStateOf<WorldLocation?>(null) }
    var mapOffset by remember { mutableStateOf(Offset.Zero) }
    var mapScale by remember { mutableStateOf(1f) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "map")
    
    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DeepLibraryBrown.copy(alpha = 0.95f),
                        DeepLibraryBrown
                    )
                )
            )
    ) {
        // Decorative border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(GildedGold, GildedGold.copy(alpha = 0.5f), GildedGold)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        )
        
        // Map title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✦ ${world.name.uppercase()} ✦",
                color = GildedGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "A Map of the Realm",
                color = AncientParchment.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
        
        // Interactive map area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, start = 24.dp, end = 24.dp, bottom = 150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AncientParchment.copy(alpha = 0.1f))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        mapOffset = Offset(
                            x = (mapOffset.x + dragAmount.x).coerceIn(-300f, 300f),
                            y = (mapOffset.y + dragAmount.y).coerceIn(-300f, 300f)
                        )
                    }
                }
        ) {
            // Map background with aged paper effect
            MapBackground()
            
            // Connection paths between locations
            ConnectionPaths(
                locations = world.locations,
                visitedLocations = visitedLocations,
                mapOffset = mapOffset
            )
            
            // Location markers
            world.locations.forEach { location ->
                LocationMarker(
                    location = location,
                    isCurrentLocation = location.id == currentLocation.id,
                    isVisited = visitedLocations.contains(location.id),
                    isSelected = selectedLocation?.id == location.id,
                    pulseAnimation = pulseAnimation,
                    mapOffset = mapOffset,
                    onClick = { selectedLocation = location }
                )
            }
            
            // Compass rose
            CompassRose(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(80.dp)
            )
        }
        
        // Location info panel
        selectedLocation?.let { location ->
            LocationInfoPanel(
                location = location,
                isVisited = visitedLocations.contains(location.id),
                isCurrent = location.id == currentLocation.id,
                onNavigate = {
                    onLocationSelected(location)
                    selectedLocation = null
                },
                onDismiss = { selectedLocation = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            )
        }
        
        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close map",
                tint = GildedGold
            )
        }
        
        // Legend
        MapLegend(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        )
    }
}

@Composable
private fun MapBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Aged paper texture simulation
        val gridSize = 20f
        val cols = (size.width / gridSize).toInt()
        val rows = (size.height / gridSize).toInt()
        
        repeat(rows) { row ->
            repeat(cols) { col ->
                val variation = Random(row * cols + col).nextFloat() * 0.05f
                drawRect(
                    color = AncientParchment.copy(alpha = 0.15f + variation),
                    topLeft = Offset(col * gridSize, row * gridSize),
                    size = Size(gridSize, gridSize)
                )
            }
        }
        
        // Grid lines for map feel
        val majorGridSize = 80f
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = DeepLibraryBrown.copy(alpha = 0.1f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 0.5f
            )
            x += majorGridSize
        }
        
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = DeepLibraryBrown.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.5f
            )
            y += majorGridSize
        }
    }
}

@Composable
private fun ConnectionPaths(
    locations: List<WorldLocation>,
    visitedLocations: Set<String>,
    mapOffset: Offset
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw paths between connected locations
        locations.forEach { location ->
            location.connectedLocations.forEach { connectedId ->
                val connectedLocation = locations.find { it.id == connectedId }
                connectedLocation?.let { target ->
                    val startX = size.width * (location.coordinates.x / 100f) + mapOffset.x
                    val startY = size.height * (location.coordinates.y / 100f) + mapOffset.y
                    val endX = size.width * (target.coordinates.x / 100f) + mapOffset.x
                    val endY = size.height * (target.coordinates.y / 100f) + mapOffset.y
                    
                    val bothVisited = visitedLocations.contains(location.id) && 
                                     visitedLocations.contains(target.id)
                    
                    // Dashed path
                    val pathColor = if (bothVisited) 
                        GildedGold.copy(alpha = 0.6f) 
                    else 
                        AncientParchment.copy(alpha = 0.3f)
                    
                    val path = Path().apply {
                        moveTo(startX, startY)
                        // Add slight curve
                        val midX = (startX + endX) / 2 + (startY - endY) * 0.1f
                        val midY = (startY + endY) / 2 + (endX - startX) * 0.1f
                        quadraticTo(midX, midY, endX, endY)
                    }
                    
                    drawPath(
                        path = path,
                        color = pathColor,
                        style = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationMarker(
    location: WorldLocation,
    isCurrentLocation: Boolean,
    isVisited: Boolean,
    isSelected: Boolean,
    pulseAnimation: Float,
    mapOffset: Offset,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    
    val x = with(density) { (mapOffset.x + location.coordinates.x * 3).toDp() }
    val y = with(density) { (mapOffset.y + location.coordinates.y * 4).toDp() }
    
    val markerSize = if (isCurrentLocation) 50.dp else 40.dp
    val scale = if (isSelected) 1.2f else 1f
    
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(markerSize * scale)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Glow for current location
        if (isCurrentLocation) {
            Canvas(
                modifier = Modifier
                    .size(markerSize * pulseAnimation)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MysticPurple.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
            }
        }
        
        // Marker icon based on location type
        val (icon, color) = getLocationIcon(location.type, isVisited)
        
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (isCurrentLocation) MysticPurple 
                            else if (isVisited) GildedGold.copy(alpha = 0.8f)
                            else DeepLibraryBrown.copy(alpha = 0.7f),
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = if (isSelected) GildedGold else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 16.sp
            )
        }
        
        // Location name
        Text(
            text = location.name,
            color = AncientParchment,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .offset(y = 22.dp)
                .width(80.dp)
        )
    }
}

private fun getLocationIcon(type: LocationType, isVisited: Boolean): Pair<String, Color> {
    @Suppress("UNUSED_PARAMETER")
    val visited = isVisited // Used for future visited state styling
    return when (type) {
        LocationType.CASTLE -> "🏰" to GildedGold
        LocationType.VILLAGE -> "🏘️" to Color(0xFF8B7355)
        LocationType.FOREST -> "🌲" to Color(0xFF228B22)
        LocationType.CITY -> "🏙️" to Color(0xFF4A4A4A)
        LocationType.PALACE -> "👑" to GildedGold
        LocationType.DUNGEON -> "⛓️" to Color(0xFF2F2F2F)
        LocationType.CAVE -> "🕳️" to Color(0xFF3D3D3D)
        LocationType.MOUNTAIN -> "⛰️" to Color(0xFF6B6B6B)
        LocationType.OCEAN -> "🌊" to Color(0xFF4169E1)
        LocationType.SHIP -> "⛵" to Color(0xFF8B4513)
        LocationType.RUINS -> "🏚️" to Color(0xFF8B7355)
        LocationType.MANSION -> "🏛️" to Color(0xFFD4AF37)
        LocationType.LIBRARY -> "📚" to MysticPurple
        LocationType.TEMPLE -> "🛕" to GildedGold
        LocationType.BATTLEFIELD -> "⚔️" to Color(0xFF8B0000)
        LocationType.GARDEN -> "🌺" to Color(0xFFFF69B4)
        LocationType.TOWER -> "🗼" to Color(0xFF696969)
        LocationType.BRIDGE -> "🌉" to Color(0xFF708090)
        LocationType.TAVERN -> "🍺" to Color(0xFFD2691E)
        LocationType.MARKET -> "🛒" to Color(0xFFFF8C00)
        LocationType.COTTAGE -> "🏠" to Color(0xFF8B7355)
        LocationType.DESERT -> "🏜️" to Color(0xFFD2B48C)
        LocationType.SCHOOL -> "🏫" to Color(0xFF4682B4)
        LocationType.SPACE_STATION -> "🛸" to Color(0xFF708090)
        LocationType.UNDERGROUND -> "🕳️" to Color(0xFF3D3D3D)
        LocationType.ISLAND -> "🏝️" to Color(0xFF3CB371)
    }
}

@Composable
private fun LocationInfoPanel(
    location: WorldLocation,
    isVisited: Boolean,
    isCurrent: Boolean,
    onNavigate: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = getLocationIcon(location.type, isVisited).first,
                        fontSize = 32.sp
                    )
                    
                    Column {
                        Text(
                            text = location.name,
                            color = GildedGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCurrent) {
                                LocationBadge("You are here", MysticPurple)
                            }
                            if (isVisited && !isCurrent) {
                                LocationBadge("Explored", GildedGold.copy(alpha = 0.7f))
                            }
                            if (!isVisited) {
                                LocationBadge("Undiscovered", Color.Gray)
                            }
                        }
                    }
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AncientParchment.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = location.description,
                color = AncientParchment.copy(alpha = 0.9f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // POI count
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "✨ ${location.pointsOfInterest.size} points of interest",
                    color = AncientParchment.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                
                if (!location.isUnlocked) {
                    Text(
                        text = "🔒 Locked location",
                        color = MysticPurple.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
            
            if (!isCurrent) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onNavigate,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MysticPurple
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Travel to ${location.name}",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CompassRose(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "compass")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )
    
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2 - 5
        
        // Outer circle
        drawCircle(
            color = GildedGold.copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = 2f)
        )
        
        // Direction points
        val directions = listOf("N", "E", "S", "W")
        directions.forEachIndexed { index, dir ->
            val angle = index * 90f - 90f
            val rad = angle * PI / 180
            
            val pointX = center.x + cos(rad).toFloat() * (radius - 15)
            val pointY = center.y + sin(rad).toFloat() * (radius - 15)
            
            // Arrow to direction
            val arrowPath = Path().apply {
                val tipX = center.x + cos(rad).toFloat() * radius
                val tipY = center.y + sin(rad).toFloat() * radius
                
                moveTo(tipX, tipY)
                
                val perpRad = rad + PI / 2
                val baseOffset = 8f
                lineTo(
                    center.x + cos(rad).toFloat() * (radius - 20) + cos(perpRad).toFloat() * baseOffset,
                    center.y + sin(rad).toFloat() * (radius - 20) + sin(perpRad).toFloat() * baseOffset
                )
                lineTo(
                    center.x + cos(rad).toFloat() * (radius - 20) - cos(perpRad).toFloat() * baseOffset,
                    center.y + sin(rad).toFloat() * (radius - 20) - sin(perpRad).toFloat() * baseOffset
                )
                close()
            }
            
            drawPath(
                arrowPath,
                color = if (dir == "N") GildedGold else GildedGold.copy(alpha = 0.5f)
            )
        }
        
        // Center decoration
        drawCircle(
            color = GildedGold,
            radius = 5f,
            center = center
        )
    }
}

@Composable
private fun MapLegend(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                DeepLibraryBrown.copy(alpha = 0.8f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Legend",
            color = GildedGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        
        LegendItem(
            icon = "🟣",
            text = "Current Location"
        )
        LegendItem(
            icon = "🟡",
            text = "Visited"
        )
        LegendItem(
            icon = "⚫",
            text = "Undiscovered"
        )
    }
}

@Composable
private fun LegendItem(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, fontSize = 10.sp)
        Text(
            text = text,
            color = AncientParchment.copy(alpha = 0.8f),
            fontSize = 10.sp
        )
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════
 * MINI MAP WIDGET
 * Compact map showing current position and nearby locations
 * ═══════════════════════════════════════════════════════════════════
 */

@Composable
fun MiniMapWidget(
    world: LiteraryWorld,
    currentLocation: WorldLocation,
    visitedLocations: Set<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "minimap")
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Card(
        modifier = modifier
            .size(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Mini map canvas
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                // Background
                drawRect(
                    color = AncientParchment.copy(alpha = 0.1f)
                )
                
                // Nearby location dots
                world.locations.take(8).forEach { location ->
                    val x = size.width * (location.coordinates.x / 100f)
                    val y = size.height * (location.coordinates.y / 100f)
                    val isCurrent = location.id == currentLocation.id
                    val isVisited = visitedLocations.contains(location.id)
                    
                    if (isCurrent) {
                        // Pulse effect for current location
                        drawCircle(
                            color = MysticPurple.copy(alpha = 0.3f),
                            radius = 12f * pulse,
                            center = Offset(x, y)
                        )
                    }
                    
                    drawCircle(
                        color = when {
                            isCurrent -> MysticPurple
                            isVisited -> GildedGold
                            else -> Color.Gray.copy(alpha = 0.5f)
                        },
                        radius = if (isCurrent) 6f else 4f,
                        center = Offset(x, y)
                    )
                }
            }
            
            // Map icon in corner
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = "Open map",
                tint = GildedGold.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(16.dp)
            )
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════
 * LOCATION TRANSITION OVERLAY
 * Animated transition between locations
 * ═══════════════════════════════════════════════════════════════════
 */

@Composable
fun LocationTransitionOverlay(
    fromLocation: WorldLocation,
    toLocation: WorldLocation,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val alpha = when {
        progress < 0.3f -> progress / 0.3f
        progress > 0.7f -> (1f - progress) / 0.3f
        else -> 1f
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alpha * 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        if (progress in 0.3f..0.7f) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Travel icon animation
                val iconProgress = (progress - 0.3f) / 0.4f
                
                Text(
                    text = when {
                        iconProgress < 0.33f -> "🚶"
                        iconProgress < 0.66f -> "🌟"
                        else -> getLocationIcon(toLocation.type, true).first
                    },
                    fontSize = 64.sp
                )
                
                Text(
                    text = "Traveling to",
                    color = AncientParchment.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
                
                Text(
                    text = toLocation.name,
                    color = GildedGold,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                
                // Progress indicator
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .width(200.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MysticPurple,
                    trackColor = DeepLibraryBrown,
                )
            }
        }
    }
}
