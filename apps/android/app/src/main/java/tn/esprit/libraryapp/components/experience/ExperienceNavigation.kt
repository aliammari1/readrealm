package tn.esprit.libraryapp.components.experience

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
 * Bottom navigation tabs for switching experience views
 */
@Composable
fun ExperienceNavigationTabs(
    selectedTab: ExperienceTab,
    onTabSelected: (ExperienceTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DeepLibraryBrown.copy(alpha = 0.95f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ExperienceTab.entries.forEach { tab ->
                NavigationTabItem(
                    tab = tab,
                    isSelected = tab == selectedTab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
private fun NavigationTabItem(
    tab: ExperienceTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = if (isSelected) GildedGold else AncientParchment.copy(alpha = 0.5f),
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) GildedGold else AncientParchment.copy(alpha = 0.5f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        
        // Selection indicator
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(GildedGold)
            )
        }
    }
}

enum class ExperienceTab(val icon: ImageVector, val label: String) {
    WORLD(Icons.Default.Explore, "World"),
    SCENES(Icons.Default.Theaters, "Scenes"),
    CHARACTERS(Icons.Default.People, "Cast"),
    MAP(Icons.Default.Map, "Map"),
    JOURNAL(Icons.Default.MenuBook, "Journal")
}

/**
 * Scene selector carousel
 */
@Composable
fun SceneSelectorCarousel(
    scenes: List<ExperienceScene>,
    currentSceneId: String?,
    onSceneSelected: (ExperienceScene) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Scenes",
            style = MaterialTheme.typography.titleMedium,
            color = GildedGold,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(scenes, key = { it.id }) { scene ->
                SceneCarouselCard(
                    scene = scene,
                    isSelected = scene.id == currentSceneId,
                    onClick = { onSceneSelected(scene) }
                )
            }
        }
    }
}

@Composable
private fun SceneCarouselCard(
    scene: ExperienceScene,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 4.dp,
        label = "elevation"
    )

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MysticPurple.copy(alpha = 0.8f) 
            else 
                DeepLibraryBrown.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            getMoodColor(scene.mood).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Scene title
                Text(
                    text = scene.sceneTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) GildedGold else AncientParchment,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mood indicator
                    MoodBadge(mood = scene.mood, isCompact = true)

                    // Key scene indicator
                    if (scene.isKeyScene) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Key Scene",
                            tint = GildedGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Selection border
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun MoodBadge(
    mood: SceneMood,
    isCompact: Boolean = false
) {
    val moodColor = getMoodColor(mood)

    Surface(
        shape = RoundedCornerShape(if (isCompact) 4.dp else 8.dp),
        color = moodColor.copy(alpha = 0.3f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = if (isCompact) 6.dp else 8.dp,
                vertical = if (isCompact) 2.dp else 4.dp
            )
        ) {
            Icon(
                imageVector = getMoodIcon(mood),
                contentDescription = null,
                tint = moodColor,
                modifier = Modifier.size(if (isCompact) 10.dp else 14.dp)
            )
            if (!isCompact) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = mood.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = moodColor
                )
            }
        }
    }
}

private fun getMoodColor(mood: SceneMood): Color {
    return when (mood) {
        SceneMood.TENSE -> Color(0xFFE63946)
        SceneMood.ROMANTIC -> Color(0xFFE91E8C)
        SceneMood.MYSTERIOUS -> Color(0xFF9C27B0)
        SceneMood.JOYFUL -> Color(0xFFFFD700)
        SceneMood.MELANCHOLIC -> Color(0xFF5C6BC0)
        SceneMood.EPIC -> Color(0xFFFF6B00)
        SceneMood.TERRIFYING -> Color(0xFF1A1A2E)
        SceneMood.PEACEFUL -> Color(0xFF81C784)
        SceneMood.CHAOTIC -> Color(0xFFD32F2F)
        SceneMood.TRIUMPHANT -> Color(0xFFD4AF37)
        SceneMood.TRAGIC -> Color(0xFF37474F)
        SceneMood.HOPEFUL -> Color(0xFF26C6DA)
        SceneMood.OMINOUS -> Color(0xFF4A0072)
    }
}

private fun getMoodIcon(mood: SceneMood): ImageVector {
    return when (mood) {
        SceneMood.TENSE -> Icons.Default.Warning
        SceneMood.ROMANTIC -> Icons.Default.Favorite
        SceneMood.MYSTERIOUS -> Icons.Default.Visibility
        SceneMood.JOYFUL -> Icons.Default.Celebration
        SceneMood.MELANCHOLIC -> Icons.Default.WaterDrop
        SceneMood.EPIC -> Icons.Default.Whatshot
        SceneMood.TERRIFYING -> Icons.Default.Dangerous
        SceneMood.PEACEFUL -> Icons.Default.Spa
        SceneMood.CHAOTIC -> Icons.Default.Bolt
        SceneMood.TRIUMPHANT -> Icons.Default.MilitaryTech
        SceneMood.TRAGIC -> Icons.Default.HeartBroken
        SceneMood.HOPEFUL -> Icons.Default.LightMode
        SceneMood.OMINOUS -> Icons.Default.Cloud
    }
}

/**
 * Character quick selector
 */
@Composable
fun CharacterQuickSelector(
    characters: List<ExperienceCharacter>,
    selectedCharacterId: String?,
    onCharacterSelected: (ExperienceCharacter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Characters",
            style = MaterialTheme.typography.titleMedium,
            color = GildedGold,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(characters, key = { it.id }) { character ->
                CharacterQuickCard(
                    character = character,
                    isSelected = character.id == selectedCharacterId,
                    onClick = { onCharacterSelected(character) }
                )
            }
        }
    }
}

@Composable
private fun CharacterQuickCard(
    character: ExperienceCharacter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // Avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected)
                        Brush.linearGradient(listOf(GildedGold, MysticPurple))
                    else
                        Brush.linearGradient(listOf(MysticPurple, DeepLibraryBrown))
                )
                .padding(3.dp)
                .clip(CircleShape)
                .background(getRoleColor(character.role))
        ) {
            Text(
                text = character.name.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AncientParchment
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Name
        Text(
            text = character.name.split(" ").first(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) GildedGold else AncientParchment.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Role badge
        Text(
            text = character.role.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            fontSize = 8.sp,
            color = getRoleColor(character.role).copy(alpha = 0.8f),
            fontStyle = FontStyle.Italic
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
        CharacterRole.GUARDIAN -> Color(0xFF00BCD4)
        CharacterRole.HERALD -> Color(0xFFFF9800)
        CharacterRole.SHAPESHIFTER -> Color(0xFF9E9E9E)
        CharacterRole.SHADOW -> Color(0xFF37474F)
        CharacterRole.ALLY -> Color(0xFF8BC34A)
        CharacterRole.TRICKSTER -> Color(0xFFFFD54F)
    }
}
