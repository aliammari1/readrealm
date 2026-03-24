package tn.esprit.libraryapp.components.experience

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tn.esprit.libraryapp.models.experience.WorldLocation

// Theme colors
private val DeepLibraryBrown = Color(0xFF2C1810)
private val GildedGold = Color(0xFFD4AF37)
private val MysticPurple = Color(0xFF4A1942)
private val AncientParchment = Color(0xFFF5E6D3)

/**
 * Location info card that appears when hovering over a location
 */
@Composable
fun LocationInfoCard(
    location: WorldLocation,
    isVisited: Boolean,
    onVisitClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.widthIn(max = 280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GildedGold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AncientParchment.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = location.description,
                style = MaterialTheme.typography.bodySmall,
                color = AncientParchment.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // POI count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = GildedGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${location.pointsOfInterest.size} points of interest",
                    style = MaterialTheme.typography.labelSmall,
                    color = AncientParchment.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visit button
            Button(
                onClick = onVisitClick,
                enabled = !isVisited,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVisited) MysticPurple.copy(alpha = 0.5f) else GildedGold,
                    contentColor = if (isVisited) AncientParchment else DeepLibraryBrown
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isVisited) Icons.Default.Check else Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isVisited) "Already Visited" else "Visit Location")
            }
        }
    }
}

/**
 * World info panel showing current world details
 */
@Composable
fun WorldInfoPanel(
    worldName: String,
    worldType: String,
    era: String,
    magicLevel: Float,
    dangerLevel: Float,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.widthIn(max = 320.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.95f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = worldName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GildedGold
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AncientParchment
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // World type badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MysticPurple.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "$worldType • $era",
                    style = MaterialTheme.typography.labelMedium,
                    color = AncientParchment,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            WorldStatBar(
                label = "Magic Level",
                value = magicLevel,
                color = Color(0xFF9C27B0),
                icon = Icons.Default.AutoAwesome
            )

            Spacer(modifier = Modifier.height(8.dp))

            WorldStatBar(
                label = "Danger Level",
                value = dangerLevel,
                color = Color(0xFFE91E63),
                icon = Icons.Default.Warning
            )
        }
    }
}

@Composable
private fun WorldStatBar(
    label: String,
    value: Float,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AncientParchment.copy(alpha = 0.7f)
                )
                Text(
                    text = "${(value * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

/**
 * Settings panel for experience customization
 */
@Composable
fun ExperienceSettingsPanel(
    ambientSoundEnabled: Boolean,
    onAmbientSoundToggle: (Boolean) -> Unit,
    particleEffectsEnabled: Boolean,
    onParticleEffectsToggle: (Boolean) -> Unit,
    narratorSpeed: Float,
    onNarratorSpeedChange: (Float) -> Unit,
    autoPlayScenes: Boolean,
    onAutoPlayToggle: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.widthIn(max = 340.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.95f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Experience Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GildedGold
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AncientParchment
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Ambient Sound
            SettingsSwitch(
                title = "Ambient Sound",
                subtitle = "Background audio & music",
                icon = Icons.Default.MusicNote,
                checked = ambientSoundEnabled,
                onCheckedChange = onAmbientSoundToggle
            )

            HorizontalDivider(
                color = MysticPurple.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Particle Effects
            SettingsSwitch(
                title = "Particle Effects",
                subtitle = "Visual atmosphere particles",
                icon = Icons.Default.AutoAwesome,
                checked = particleEffectsEnabled,
                onCheckedChange = onParticleEffectsToggle
            )

            HorizontalDivider(
                color = MysticPurple.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Auto-play Scenes
            SettingsSwitch(
                title = "Auto-play Scenes",
                subtitle = "Automatically progress dialogues",
                icon = Icons.Default.PlayCircle,
                checked = autoPlayScenes,
                onCheckedChange = onAutoPlayToggle
            )

            HorizontalDivider(
                color = MysticPurple.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Narrator Speed
            SettingsSlider(
                title = "Narrator Speed",
                value = narratorSpeed,
                onValueChange = onNarratorSpeedChange,
                icon = Icons.Default.Speed
            )
        }
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GildedGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AncientParchment
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = AncientParchment.copy(alpha = 0.5f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = GildedGold,
                checkedTrackColor = MysticPurple,
                uncheckedThumbColor = AncientParchment.copy(alpha = 0.5f),
                uncheckedTrackColor = MysticPurple.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun SettingsSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GildedGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = AncientParchment
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = GildedGold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.5f..2f,
            colors = SliderDefaults.colors(
                thumbColor = GildedGold,
                activeTrackColor = GildedGold,
                inactiveTrackColor = MysticPurple.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * Exit confirmation dialog
 */
@Composable
fun ExitExperienceDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    progress: Float
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepLibraryBrown,
        titleContentColor = GildedGold,
        textContentColor = AncientParchment,
        title = {
            Text(
                text = "Leave Experience?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Your progress (${(progress * 100).toInt()}%) will be saved.")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You can continue your journey anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AncientParchment.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Leave", color = Color(0xFFE91E63))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Stay", color = GildedGold)
            }
        }
    )
}
