package tn.esprit.libraryapp.screens.experience

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import tn.esprit.libraryapp.components.experience.*
import tn.esprit.libraryapp.models.experience.*

// Theme colors
private val DeepLibraryBrown = Color(0xFF2C1810)
private val GildedGold = Color(0xFFD4AF37)
private val MysticPurple = Color(0xFF4A1942)
private val AncientParchment = Color(0xFFF5E6D3)

/**
 * World exploration tab content
 */
@Composable
fun WorldTabContent(
    world: LiteraryWorld,
    currentLocation: WorldLocation?,
    onLocationSelected: (WorldLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // World header info
        WorldHeaderCard(world = world)

        Spacer(modifier = Modifier.height(16.dp))

        // Current location
        if (currentLocation != null) {
            CurrentLocationCard(
                location = currentLocation,
                onExplore = { /* Already here */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nearby locations
        Text(
            text = "Nearby Locations",
            style = MaterialTheme.typography.titleMedium,
            color = GildedGold,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        val nearbyLocations = currentLocation?.let { loc ->
            world.locations.filter { it.id in loc.connectedLocations }
        } ?: world.locations.take(5)
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nearbyLocations, key = { it.id }) { location ->
                NearbyLocationCard(
                    location = location,
                    onClick = { onLocationSelected(location) }
                )
            }
        }
    }
}

@Composable
private fun WorldHeaderCard(world: LiteraryWorld) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MysticPurple.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = world.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = GildedGold
            )
            Text(
                text = world.description,
                style = MaterialTheme.typography.bodyMedium,
                color = AncientParchment.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoChip(
                    icon = Icons.Default.Public,
                    text = world.type.name.replace("_", " ")
                )
                InfoChip(
                    icon = Icons.Default.History,
                    text = world.era.name.replace("_", " ")
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DeepLibraryBrown.copy(alpha = 0.6f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GildedGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = AncientParchment
            )
        }
    }
}

@Composable
private fun CurrentLocationCard(
    location: WorldLocation,
    onExplore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GildedGold.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = GildedGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Location",
                        style = MaterialTheme.typography.labelSmall,
                        color = GildedGold
                    )
                }
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AncientParchment
                )
                Text(
                    text = "${location.pointsOfInterest.size} POIs to discover",
                    style = MaterialTheme.typography.bodySmall,
                    color = AncientParchment.copy(alpha = 0.6f)
                )
            }

            FilledTonalButton(
                onClick = onExplore,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = GildedGold,
                    contentColor = DeepLibraryBrown
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Explore")
            }
        }
    }
}

@Composable
private fun NearbyLocationCard(
    location: WorldLocation,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MysticPurple.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = getLocationIcon(location.type),
                    contentDescription = null,
                    tint = GildedGold,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = AncientParchment
                )
                Text(
                    text = location.type.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = AncientParchment.copy(alpha = 0.5f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AncientParchment.copy(alpha = 0.5f)
            )
        }
    }
}

private fun getLocationIcon(type: LocationType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        LocationType.CASTLE -> Icons.Default.Castle
        LocationType.VILLAGE -> Icons.Default.Home
        LocationType.FOREST -> Icons.Default.Forest
        LocationType.CITY -> Icons.Default.LocationCity
        LocationType.PALACE -> Icons.Default.Castle
        LocationType.CAVE -> Icons.Default.Terrain
        LocationType.MOUNTAIN -> Icons.Default.Terrain
        LocationType.OCEAN -> Icons.Default.Water
        LocationType.SHIP -> Icons.Default.DirectionsBoat
        LocationType.RUINS -> Icons.Default.Foundation
        LocationType.MANSION -> Icons.Default.House
        LocationType.LIBRARY -> Icons.Default.LocalLibrary
        LocationType.TEMPLE -> Icons.Default.Church
        LocationType.GARDEN -> Icons.Default.Park
        LocationType.TOWER -> Icons.Default.CellTower
        LocationType.TAVERN -> Icons.Default.LocalBar
        LocationType.MARKET -> Icons.Default.Store
        else -> Icons.Default.Place
    }
}

/**
 * Scenes list tab content
 */
@Composable
fun ScenesTabContent(
    scenes: List<ExperienceScene>,
    currentSceneId: String?,
    completedSceneIds: Set<String>,
    onSceneClick: (ExperienceScene) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(scenes, key = { it.id }) { scene ->
            SceneListCard(
                scene = scene,
                isCurrent = scene.id == currentSceneId,
                isCompleted = scene.id in completedSceneIds,
                onClick = { onSceneClick(scene) }
            )
        }
    }
}

@Composable
private fun SceneListCard(
    scene: ExperienceScene,
    isCurrent: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        isCurrent -> GildedGold.copy(alpha = 0.3f)
        isCompleted -> MysticPurple.copy(alpha = 0.3f)
        else -> DeepLibraryBrown.copy(alpha = 0.6f)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isCurrent -> GildedGold.copy(alpha = 0.5f)
                            isCompleted -> Color(0xFF4CAF50).copy(alpha = 0.5f)
                            else -> MysticPurple.copy(alpha = 0.3f)
                        }
                    )
            ) {
                Icon(
                    imageVector = when {
                        isCurrent -> Icons.Default.PlayArrow
                        isCompleted -> Icons.Default.Check
                        else -> Icons.Default.Theaters
                    },
                    contentDescription = null,
                    tint = when {
                        isCurrent -> DeepLibraryBrown
                        isCompleted -> Color.White
                        else -> AncientParchment
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = scene.sceneTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCurrent) GildedGold else AncientParchment
                    )
                    if (scene.isKeyScene) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Key Scene",
                            tint = GildedGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoodBadge(mood = scene.mood, isCompact = true)
                    Text(
                        text = "${scene.keyEvents.size} events",
                        style = MaterialTheme.typography.labelSmall,
                        color = AncientParchment.copy(alpha = 0.5f)
                    )
                }
            }

            if (isCurrent) {
                Text(
                    text = "PLAYING",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GildedGold
                )
            }
        }
    }
}

/**
 * Characters gallery tab content
 */
@Composable
fun CharactersTabContent(
    characters: List<ExperienceCharacter>,
    selectedCharacterId: String?,
    metCharacterIds: Set<String>,
    onCharacterClick: (ExperienceCharacter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(characters, key = { it.id }) { character ->
            CharacterListCard(
                character = character,
                isSelected = character.id == selectedCharacterId,
                isMet = character.id in metCharacterIds,
                onClick = { onCharacterClick(character) }
            )
        }
    }
}

@Composable
private fun CharacterListCard(
    character: ExperienceCharacter,
    isSelected: Boolean,
    isMet: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                GildedGold.copy(alpha = 0.2f) 
            else 
                DeepLibraryBrown.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            CharacterAvatar(
                character = character,
                size = 56,
                showBorder = isSelected
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) GildedGold else AncientParchment
                )

                Text(
                    text = character.role.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = AncientParchment.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoleBadge(role = character.role)
                    if (!isMet) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF607D8B).copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "Not met",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF607D8B),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AncientParchment.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun CharacterAvatar(
    character: ExperienceCharacter,
    size: Int,
    showBorder: Boolean
) {
    val roleColor = getRoleColor(character.role)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(size / 4))
            .then(
                if (showBorder) {
                    Modifier.background(
                        Brush.linearGradient(listOf(GildedGold, MysticPurple))
                    ).padding(2.dp).clip(RoundedCornerShape(size / 4 - 2))
                } else Modifier
            )
            .background(roleColor)
    ) {
        Text(
            text = character.name.take(2).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AncientParchment
        )
    }
}

@Composable
private fun RoleBadge(role: CharacterRole) {
    val color = getRoleColor(role)
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.3f)
    ) {
        Text(
            text = role.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun getRoleColor(role: CharacterRole): Color {
    return when (role) {
        CharacterRole.PROTAGONIST -> Color(0xFF2196F3)
        CharacterRole.ANTAGONIST -> Color(0xFFE91E63)
        CharacterRole.DEUTERAGONIST -> Color(0xFF4CAF50)
        CharacterRole.MENTOR -> Color(0xFFD4AF37)
        CharacterRole.SIDEKICK -> Color(0xFF9C27B0)
        CharacterRole.LOVE_INTEREST -> Color(0xFFFF4081)
        CharacterRole.COMIC_RELIEF -> Color(0xFFFFEB3B)
        CharacterRole.GUARDIAN -> Color(0xFF607D8B)
        CharacterRole.HERALD -> Color(0xFF795548)
        CharacterRole.SHAPESHIFTER -> Color(0xFF00BCD4)
        CharacterRole.SHADOW -> Color(0xFF424242)
        CharacterRole.ALLY -> Color(0xFF8BC34A)
        CharacterRole.TRICKSTER -> Color(0xFFFF9800)
    }
}
