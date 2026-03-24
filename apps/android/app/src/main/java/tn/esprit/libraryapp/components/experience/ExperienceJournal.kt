package tn.esprit.libraryapp.components.experience

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.libraryapp.models.experience.*

// Theme colors
private val DeepLibraryBrown = Color(0xFF2C1810)
private val GildedGold = Color(0xFFD4AF37)
private val MysticPurple = Color(0xFF4A1942)
private val AncientParchment = Color(0xFFF5E6D3)
private val EnchantedForestGreen = Color(0xFF1B4332)

/**
 * Journal entry for tracking discoveries
 */
data class JournalEntry(
    val id: String,
    val type: JournalEntryType,
    val title: String,
    val description: String,
    val timestamp: Long,
    val relatedId: String? = null
)

enum class JournalEntryType {
    LOCATION_VISITED,
    CHARACTER_MET,
    SCENE_WITNESSED,
    POI_DISCOVERED,
    ACHIEVEMENT_UNLOCKED
}

/**
 * Experience journal panel
 */
@Composable
fun ExperienceJournal(
    entries: List<JournalEntry>,
    onEntryClick: (JournalEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepLibraryBrown.copy(alpha = 0.95f))
    ) {
        // Header
        JournalHeader(totalEntries = entries.size)

        // Entries list
        if (entries.isEmpty()) {
            EmptyJournalState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    JournalEntryCard(
                        entry = entry,
                        onClick = { onEntryClick(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalHeader(totalEntries: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MysticPurple.copy(alpha = 0.5f), Color.Transparent)
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Adventure Journal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GildedGold
                )
                Text(
                    text = "$totalEntries discoveries",
                    style = MaterialTheme.typography.bodySmall,
                    color = AncientParchment.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = GildedGold,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun EmptyJournalState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                tint = GildedGold.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your journey awaits",
                style = MaterialTheme.typography.titleMedium,
                color = AncientParchment.copy(alpha = 0.5f)
            )
            Text(
                text = "Explore the world to fill your journal",
                style = MaterialTheme.typography.bodySmall,
                color = AncientParchment.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MysticPurple.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(getEntryTypeColor(entry.type).copy(alpha = 0.3f))
            ) {
                Icon(
                    imageVector = getEntryTypeIcon(entry.type),
                    contentDescription = null,
                    tint = getEntryTypeColor(entry.type),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AncientParchment,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AncientParchment.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatTimestamp(entry.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = GildedGold.copy(alpha = 0.6f),
                    fontStyle = FontStyle.Italic
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AncientParchment.copy(alpha = 0.5f)
            )
        }
    }
}

private fun getEntryTypeColor(type: JournalEntryType): Color {
    return when (type) {
        JournalEntryType.LOCATION_VISITED -> Color(0xFF4CAF50)
        JournalEntryType.CHARACTER_MET -> Color(0xFF2196F3)
        JournalEntryType.SCENE_WITNESSED -> Color(0xFF9C27B0)
        JournalEntryType.POI_DISCOVERED -> Color(0xFFD4AF37)
        JournalEntryType.ACHIEVEMENT_UNLOCKED -> Color(0xFFFF9800)
    }
}

private fun getEntryTypeIcon(type: JournalEntryType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        JournalEntryType.LOCATION_VISITED -> Icons.Default.Place
        JournalEntryType.CHARACTER_MET -> Icons.Default.Person
        JournalEntryType.SCENE_WITNESSED -> Icons.Default.Theaters
        JournalEntryType.POI_DISCOVERED -> Icons.Default.Stars
        JournalEntryType.ACHIEVEMENT_UNLOCKED -> Icons.Default.EmojiEvents
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> "${diff / 86_400_000}d ago"
    }
}

/**
 * Stats summary panel
 */
@Composable
fun ExperienceStatsSummary(
    visitedLocations: Int,
    totalLocations: Int,
    metCharacters: Int,
    totalCharacters: Int,
    completedScenes: Int,
    totalScenes: Int,
    discoveredPois: Int,
    totalPois: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Journey Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GildedGold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Place,
                    value = visitedLocations,
                    total = totalLocations,
                    label = "Locations",
                    color = Color(0xFF4CAF50)
                )
                StatItem(
                    icon = Icons.Default.People,
                    value = metCharacters,
                    total = totalCharacters,
                    label = "Characters",
                    color = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Theaters,
                    value = completedScenes,
                    total = totalScenes,
                    label = "Scenes",
                    color = Color(0xFF9C27B0)
                )
                StatItem(
                    icon = Icons.Default.Stars,
                    value = discoveredPois,
                    total = totalPois,
                    label = "Discoveries",
                    color = GildedGold
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Int,
    total: Int,
    label: String,
    color: Color
) {
    val progress = if (total > 0) value.toFloat() / total else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(56.dp),
                color = color,
                trackColor = color.copy(alpha = 0.2f),
                strokeWidth = 4.dp
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$value / $total",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = AncientParchment
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AncientParchment.copy(alpha = 0.6f)
        )
    }
}

/**
 * Achievement popup
 */
@Composable
fun AchievementPopup(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            }
            .widthIn(max = 300.dp)
            .clickable(onClick = onDismiss),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Trophy icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GildedGold, MysticPurple)
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = AncientParchment,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Achievement Unlocked!",
                style = MaterialTheme.typography.labelMedium,
                color = GildedGold,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AncientParchment,
                textAlign = TextAlign.Center
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AncientParchment.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tap to dismiss",
                style = MaterialTheme.typography.labelSmall,
                color = AncientParchment.copy(alpha = 0.4f)
            )
        }
    }
}
