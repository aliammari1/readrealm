package tn.esprit.libraryapp.components.experience

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.libraryapp.models.experience.*
import tn.esprit.libraryapp.ui.theme.*
import kotlin.math.*

/**
 * ═══════════════════════════════════════════════════════════════════
 * EXPERIENCE HUD (Heads-Up Display)
 * Immersive UI overlay with controls and information
 * ═══════════════════════════════════════════════════════════════════
 */

@Composable
fun ExperienceHUD(
    experience: BookExperience,
    currentLocation: WorldLocation?,
    session: ExperienceSession,
    onOpenMap: () -> Unit,
    onOpenCharacters: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleAudio: () -> Unit,
    isAudioEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Top bar with world info
        TopInfoBar(
            worldName = experience.world.name,
            locationName = currentLocation?.name ?: "Unknown",
            session = session,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // Left side quick actions
        QuickActionsPanel(
            onOpenMap = onOpenMap,
            onOpenCharacters = onOpenCharacters,
            onOpenJournal = onOpenJournal,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        
        // Right side info widgets
        InfoWidgetsPanel(
            currentLocation = currentLocation,
            timeOfDay = experience.world.timeOfDay,
            magicLevel = experience.world.magicLevel,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
        
        // Bottom controls
        BottomControlsBar(
            session = session,
            onOpenSettings = onOpenSettings,
            onToggleAudio = onToggleAudio,
            isAudioEnabled = isAudioEnabled,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TopInfoBar(
    worldName: String,
    locationName: String,
    session: ExperienceSession,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.7f),
                        Color.Transparent
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // World & Location
            Column {
                Text(
                    text = worldName,
                    color = GildedGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MysticPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = locationName,
                        color = AncientParchment.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
            
            // Session progress
            SessionProgressBadge(session = session)
        }
    }
}

@Composable
private fun SessionProgressBadge(session: ExperienceSession) {
    // Calculate progress locally since ExperienceSession doesn't have a progress field
    val visitedCount = session.visitedLocations.size
    val discoveredCount = session.discoveredPois.size
    val completedCount = session.completedInteractions.size
    // Calculate a simple progress estimate
    val estimatedProgress = minOf(1f, (visitedCount + discoveredCount + completedCount) / 30f)
    val progressPercent = (estimatedProgress * 100).toInt()
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Discoveries
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$discoveredCount",
                color = GildedGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Found",
                color = AncientParchment.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
        
        // Progress ring
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { estimatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = MysticPurple,
                strokeWidth = 3.dp,
                trackColor = DeepLibraryBrown.copy(alpha = 0.5f),
            )
            
            Text(
                text = "$progressPercent%",
                color = AncientParchment,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Time spent
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val minutes = session.totalTimeSpent / 60000
            Text(
                text = "${minutes}m",
                color = GildedGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Time",
                color = AncientParchment.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun QuickActionsPanel(
    onOpenMap: () -> Unit,
    onOpenCharacters: () -> Unit,
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .background(
                DeepLibraryBrown.copy(alpha = 0.8f),
                RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = GildedGold.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            icon = Icons.Default.Map,
            label = "Map",
            onClick = onOpenMap
        )
        
        QuickActionButton(
            icon = Icons.Default.People,
            label = "Cast",
            onClick = onOpenCharacters
        )
        
        QuickActionButton(
            icon = Icons.Default.Book,
            label = "Journal",
            onClick = onOpenJournal
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = GildedGold,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = AncientParchment.copy(alpha = 0.8f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun InfoWidgetsPanel(
    currentLocation: WorldLocation?,
    timeOfDay: TimeOfDay,
    magicLevel: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Time of day indicator
        TimeOfDayWidget(timeOfDay = timeOfDay)
        
        // Magic level indicator
        if (magicLevel > 0.1f) {
            MagicLevelWidget(level = magicLevel)
        }
        
        // Location POI count
        currentLocation?.let { location ->
            if (location.pointsOfInterest.isNotEmpty()) {
                PoiCountWidget(
                    total = location.pointsOfInterest.size,
                    discovered = location.pointsOfInterest.count { it.isDiscovered }
                )
            }
        }
    }
}

@Composable
private fun TimeOfDayWidget(timeOfDay: TimeOfDay) {
    val (icon, label) = when (timeOfDay) {
        TimeOfDay.DAWN -> "🌅" to "Dawn"
        TimeOfDay.MORNING -> "☀️" to "Morning"
        TimeOfDay.NOON -> "🌞" to "Noon"
        TimeOfDay.AFTERNOON -> "⛅" to "Afternoon"
        TimeOfDay.DUSK -> "🌆" to "Dusk"
        TimeOfDay.EVENING -> "🌇" to "Evening"
        TimeOfDay.NIGHT -> "🌙" to "Night"
        TimeOfDay.MIDNIGHT -> "🌑" to "Midnight"
        else -> "⏰" to "Time"
    }
    
    Row(
        modifier = Modifier
            .background(
                DeepLibraryBrown.copy(alpha = 0.8f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = label,
            color = AncientParchment,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun MagicLevelWidget(level: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "magic")
    
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "magicGlow"
    )
    
    Row(
        modifier = Modifier
            .background(
                MysticPurple.copy(alpha = 0.3f * glow),
                RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = MysticPurple.copy(alpha = glow * 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🔮", fontSize = 16.sp)
        
        // Magic level bar
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(6.dp)
                .background(
                    DeepLibraryBrown,
                    RoundedCornerShape(3.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(level)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(MysticPurple, GildedGold)
                        ),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@Composable
private fun PoiCountWidget(total: Int, discovered: Int) {
    Row(
        modifier = Modifier
            .background(
                DeepLibraryBrown.copy(alpha = 0.8f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "✨", fontSize = 16.sp)
        Text(
            text = "$discovered/$total",
            color = if (discovered == total) GildedGold else AncientParchment,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BottomControlsBar(
    session: ExperienceSession,
    onOpenSettings: () -> Unit,
    onToggleAudio: () -> Unit,
    isAudioEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.7f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Audio toggle
            IconButton(
                onClick = onToggleAudio,
                modifier = Modifier
                    .background(
                        DeepLibraryBrown.copy(alpha = 0.8f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isAudioEnabled) 
                        Icons.Default.VolumeUp 
                    else 
                        Icons.Default.VolumeOff,
                    contentDescription = "Toggle audio",
                    tint = if (isAudioEnabled) GildedGold else AncientParchment.copy(alpha = 0.5f)
                )
            }
            
            // Interaction hint
            InteractionHint()
            
            // Settings
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .background(
                        DeepLibraryBrown.copy(alpha = 0.8f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = AncientParchment.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun InteractionHint() {
    val infiniteTransition = rememberInfiniteTransition(label = "hint")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintAlpha"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = null,
            tint = AncientParchment.copy(alpha = alpha),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "Tap to interact • Drag to explore",
            color = AncientParchment.copy(alpha = alpha),
            fontSize = 12.sp
        )
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════
 * DISCOVERY JOURNAL
 * Log of all discoveries and lore entries
 * ═══════════════════════════════════════════════════════════════════
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryJournal(
    experience: BookExperience,
    session: ExperienceSession,
    onClose: () -> Unit,
    onEntryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepLibraryBrown)
    ) {
        // Decorative journal page texture
        JournalBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📔 Discovery Journal",
                        color = GildedGold,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = experience.bookTitle,
                        color = AncientParchment.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
                
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = GildedGold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats summary
            JournalStats(
                locationsVisited = session.visitedLocations.size,
                totalLocations = experience.world.locations.size,
                poisDiscovered = session.discoveredPois.size,
                scenesCompleted = session.completedInteractions.size,
                totalScenes = experience.scenes.size
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Journal entries
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Locations section
                JournalSection(
                    title = "Explored Locations",
                    icon = "🗺️"
                ) {
                    session.visitedLocations.forEach { locationId ->
                        val location = experience.world.locations.find { it.id == locationId }
                        location?.let {
                            JournalLocationEntry(
                                location = it,
                                onClick = { onEntryClick(locationId) }
                            )
                        }
                    }
                    
                    if (session.visitedLocations.isEmpty()) {
                        EmptyJournalEntry(text = "No locations explored yet")
                    }
                }
                
                // Discoveries section
                JournalSection(
                    title = "Discoveries",
                    icon = "✨"
                ) {
                    session.discoveredPois.take(10).forEach { poiId ->
                        val poi = experience.world.locations
                            .flatMap { it.pointsOfInterest }
                            .find { it.id == poiId }
                        
                        poi?.let {
                            JournalPoiEntry(
                                poi = it,
                                onClick = { onEntryClick(poiId) }
                            )
                        }
                    }
                    
                    if (session.discoveredPois.isEmpty()) {
                        EmptyJournalEntry(text = "No discoveries yet")
                    }
                }
                
                // Scenes section
                JournalSection(
                    title = "Story Moments",
                    icon = "🎭"
                ) {
                    session.completedInteractions.forEach { sceneId ->
                        val scene = experience.scenes.find { it.id == sceneId }
                        scene?.let {
                            JournalSceneEntry(
                                scene = it,
                                onClick = { onEntryClick(sceneId) }
                            )
                        }
                    }
                    
                    if (session.completedInteractions.isEmpty()) {
                        EmptyJournalEntry(text = "No scenes completed yet")
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun JournalBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Page lines
        var y = 50f
        while (y < size.height) {
            drawLine(
                color = GildedGold.copy(alpha = 0.05f),
                start = Offset(50f, y),
                end = Offset(size.width - 50f, y),
                strokeWidth = 0.5f
            )
            y += 30f
        }
        
        // Page edge decoration
        drawLine(
            color = GildedGold.copy(alpha = 0.2f),
            start = Offset(40f, 0f),
            end = Offset(40f, size.height),
            strokeWidth = 2f
        )
    }
}

@Composable
private fun JournalStats(
    locationsVisited: Int,
    totalLocations: Int,
    poisDiscovered: Int,
    scenesCompleted: Int,
    totalScenes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, GildedGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn(
                value = "$locationsVisited/$totalLocations",
                label = "Locations",
                icon = "🗺️"
            )
            StatColumn(
                value = "$poisDiscovered",
                label = "Discoveries",
                icon = "✨"
            )
            StatColumn(
                value = "$scenesCompleted/$totalScenes",
                label = "Scenes",
                icon = "🎭"
            )
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    label: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = GildedGold,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = AncientParchment.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun JournalSection(
    title: String,
    icon: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = icon, fontSize = 24.sp)
            Text(
                text = title,
                color = GildedGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        content()
    }
}

@Composable
private fun JournalLocationEntry(
    location: WorldLocation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MysticPurple.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getLocationEmoji(location.type),
                    fontSize = 20.sp
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    color = AncientParchment,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${location.pointsOfInterest.size} points of interest",
                    color = AncientParchment.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GildedGold.copy(alpha = 0.5f)
            )
        }
    }
}

private fun getLocationEmoji(type: LocationType): String {
    return when (type) {
        LocationType.CASTLE -> "🏰"
        LocationType.FOREST -> "🌲"
        LocationType.VILLAGE -> "🏘️"
        LocationType.CITY -> "🏙️"
        LocationType.MOUNTAIN -> "⛰️"
        LocationType.CAVE -> "🕳️"
        LocationType.OCEAN -> "🌊"
        LocationType.LIBRARY -> "📚"
        LocationType.TEMPLE -> "🛕"
        LocationType.GARDEN -> "🌺"
        LocationType.DUNGEON -> "⚰️"
        LocationType.MANSION -> "🏛️"
        LocationType.PALACE -> "👑"
        LocationType.TOWER -> "🗼"
        LocationType.RUINS -> "🏚️"
        else -> "📍"
    }
}

@Composable
private fun JournalPoiEntry(
    poi: PointOfInterest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = getPoiEmoji(poi.type),
                fontSize = 24.sp
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = poi.name,
                    color = AncientParchment,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = poi.description,
                    color = AncientParchment.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun getPoiEmoji(type: PoiType): String {
    return when (type) {
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
}

@Composable
private fun JournalSceneEntry(
    scene: ExperienceScene,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        GildedGold.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎬", fontSize = 20.sp)
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scene.sceneTitle,
                    color = AncientParchment,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Chapter ${scene.chapterNumber}: ${scene.chapterTitle}",
                    color = AncientParchment.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            
            if (scene.isKeyScene) {
                Box(
                    modifier = Modifier
                        .background(
                            GildedGold.copy(alpha = 0.2f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Key",
                        color = GildedGold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyJournalEntry(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = AncientParchment.copy(alpha = 0.4f),
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic
        )
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════
 * EXPERIENCE LOADING SCREEN
 * Immersive loading with world generation progress
 * ═══════════════════════════════════════════════════════════════════
 */

@Composable
fun ExperienceLoadingScreen(
    bookTitle: String,
    progress: Float,
    currentTask: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val portalRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepLibraryBrown),
        contentAlignment = Alignment.Center
    ) {
        // Magical background particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(50) { i ->
                val x = size.width * ((i * 37) % 100) / 100
                val y = size.height * ((i * 53) % 100) / 100
                val alpha = (sin((portalRotation * PI / 180 + i).toFloat()) + 1) / 2 * 0.3f
                
                drawCircle(
                    color = MysticPurple.copy(alpha = alpha),
                    radius = 2f + (i % 4),
                    center = Offset(x, y)
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Magical portal animation
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(portalRotation)
                ) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                MysticPurple.copy(alpha = 0.8f),
                                GildedGold.copy(alpha = 0.8f),
                                MysticPurple.copy(alpha = 0.8f),
                                GildedGold.copy(alpha = 0.8f)
                            )
                        ),
                        radius = size.width / 2 - 10,
                        style = Stroke(width = 4f)
                    )
                }
                
                // Inner portal
                Canvas(
                    modifier = Modifier
                        .size(150.dp)
                        .rotate(-portalRotation * 0.5f)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Black,
                                DeepLibraryBrown,
                                MysticPurple.copy(alpha = 0.5f * glowPulse),
                                Color.Transparent
                            )
                        )
                    )
                }
                
                // Center icon
                Text(
                    text = "📖",
                    fontSize = 48.sp
                )
            }
            
            // Title
            Text(
                text = "Entering the World of",
                color = AncientParchment.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            
            Text(
                text = bookTitle,
                color = GildedGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 48.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MysticPurple,
                    trackColor = DeepLibraryBrown.copy(alpha = 0.5f),
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = currentTask,
                    color = AncientParchment.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = GildedGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
