package tn.esprit.libraryapp.components.experience

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Theme colors
private val DeepLibraryBrown = Color(0xFF2C1810)
private val GildedGold = Color(0xFFD4AF37)
private val MysticPurple = Color(0xFF4A1942)
private val AncientParchment = Color(0xFFF5E6D3)

/**
 * Top header for the experience screen
 */
@Composable
fun ExperienceHeader(
    bookTitle: String,
    worldName: String,
    progress: Float,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shimmerAnimation = rememberInfiniteTransition(label = "headerShimmer")
    val shimmerAlpha by shimmerAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepLibraryBrown,
                        DeepLibraryBrown.copy(alpha = 0.95f),
                        Color.Transparent
                    )
                )
            )
            .padding(top = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Top row with navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MysticPurple.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GildedGold
                    )
                }

                // Title section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = worldName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GildedGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = bookTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AncientParchment.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MysticPurple.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = AncientParchment
                        )
                    }
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MysticPurple.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = AncientParchment
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            ExperienceProgressBar(
                progress = progress,
                shimmerAlpha = shimmerAlpha
            )
        }
    }
}

@Composable
private fun ExperienceProgressBar(
    progress: Float,
    shimmerAlpha: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Journey Progress",
                style = MaterialTheme.typography.labelSmall,
                color = AncientParchment.copy(alpha = 0.6f)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = GildedGold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MysticPurple.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                GildedGold.copy(alpha = shimmerAlpha),
                                GildedGold,
                                GildedGold.copy(alpha = shimmerAlpha)
                            )
                        )
                    )
            )
        }
    }
}

/**
 * Floating action menu for quick actions
 */
@Composable
fun ExperienceQuickActions(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onMapClick: () -> Unit,
    onCharactersClick: () -> Unit,
    onScenesClick: () -> Unit,
    onJournalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAnimation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.padding(16.dp)
    ) {
        // Expandable actions
        androidx.compose.animation.AnimatedVisibility(
            visible = isExpanded,
            enter = androidx.compose.animation.fadeIn() + 
                    androidx.compose.animation.slideInVertically { it },
            exit = androidx.compose.animation.fadeOut() + 
                   androidx.compose.animation.slideOutVertically { it }
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Map,
                    label = "Map",
                    onClick = onMapClick
                )
                QuickActionButton(
                    icon = Icons.Default.People,
                    label = "Characters",
                    onClick = onCharactersClick
                )
                QuickActionButton(
                    icon = Icons.Default.Theaters,
                    label = "Scenes",
                    onClick = onScenesClick
                )
                QuickActionButton(
                    icon = Icons.Default.Book,
                    label = "Journal",
                    onClick = onJournalClick
                )
            }
        }

        // Main FAB
        FloatingActionButton(
            onClick = onToggle,
            containerColor = GildedGold,
            contentColor = DeepLibraryBrown,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isExpanded) "Close" else "Open menu",
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotationAnimation
                }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DeepLibraryBrown.copy(alpha = 0.9f),
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AncientParchment,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        FloatingActionButton(
            onClick = onClick,
            containerColor = MysticPurple,
            contentColor = GildedGold,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
