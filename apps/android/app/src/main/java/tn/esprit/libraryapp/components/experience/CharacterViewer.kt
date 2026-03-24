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
 * CHARACTER VIEWER
 * Detailed character profiles with lore and interactions
 * ═══════════════════════════════════════════════════════════════════
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterViewer(
    character: ExperienceCharacter,
    onClose: () -> Unit,
    onQuoteSelected: (CharacterQuote) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepLibraryBrown)
    ) {
        // Background pattern
        CharacterBackgroundPattern(role = character.role)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header with portrait
            CharacterHeader(
                character = character,
                onClose = onClose
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Character details
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Quick stats
                CharacterQuickStats(character = character)
                
                // Physical description
                CharacterPhysicalDescription(description = character.physicalDescription)
                
                // Personality traits
                CharacterPersonalitySection(
                    traits = character.personality
                )
                
                // Description/Backstory
                CharacterBackstory(backstory = character.description)
                
                // Memorable quotes
                if (character.quotes.isNotEmpty()) {
                    CharacterQuotes(
                        quotes = character.quotes,
                        onQuoteSelected = onQuoteSelected
                    )
                }
                
                // Relationships
                if (character.relationships.isNotEmpty()) {
                    CharacterRelationships(relationships = character.relationships)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun CharacterBackgroundPattern(role: CharacterRole) {
    val color = when (role) {
        CharacterRole.PROTAGONIST -> Color(0xFF1A3D5C)
        CharacterRole.ANTAGONIST -> Color(0xFF3D1A1A)
        CharacterRole.MENTOR -> Color(0xFF1A3D1A)
        CharacterRole.LOVE_INTEREST -> Color(0xFF3D1A3D)
        CharacterRole.TRICKSTER -> Color(0xFF3D3D1A)
        else -> Color(0xFF1A1A3D)
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Subtle pattern based on archetype
        val patternSize = 60f
        val cols = (size.width / patternSize).toInt() + 1
        val rows = (size.height / patternSize).toInt() + 1
        
        repeat(rows) { row ->
            repeat(cols) { col ->
                val x = col * patternSize
                val y = row * patternSize
                val alpha = if ((row + col) % 2 == 0) 0.03f else 0.01f
                
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = patternSize / 3,
                    center = Offset(x + patternSize / 2, y + patternSize / 2)
                )
            }
        }
    }
}

@Composable
private fun CharacterHeader(
    character: ExperienceCharacter,
    onClose: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header")
    
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DeepLibraryBrown
                        ),
                        startY = 150f
                    )
                )
        )
        
        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = GildedGold
            )
        }
        
        // Character portrait area
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large portrait with archetype icon
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .border(
                        width = 4.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                GildedGold.copy(alpha = glowPulse),
                                MysticPurple.copy(alpha = glowPulse),
                                GildedGold.copy(alpha = glowPulse)
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(4.dp)
                    .background(DeepLibraryBrown, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getRoleIcon(character.role),
                    fontSize = 64.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Name
            Text(
                text = character.name,
                color = GildedGold,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            
            // Role display
            Text(
                text = character.role.name.replace("_", " ").lowercase()
                    .replaceFirstChar { it.uppercase() },
                color = AncientParchment.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Role badge
            RoleBadge(role = character.role)
        }
    }
}

private fun getRoleIcon(role: CharacterRole): String {
    return when (role) {
        CharacterRole.PROTAGONIST -> "⚔️"
        CharacterRole.ANTAGONIST -> "😈"
        CharacterRole.DEUTERAGONIST -> "🌟"
        CharacterRole.MENTOR -> "🧙"
        CharacterRole.SIDEKICK -> "🤝"
        CharacterRole.LOVE_INTEREST -> "💕"
        CharacterRole.COMIC_RELIEF -> "🎭"
        CharacterRole.GUARDIAN -> "🛡️"
        CharacterRole.HERALD -> "📯"
        CharacterRole.SHAPESHIFTER -> "🌙"
        CharacterRole.SHADOW -> "👤"
        CharacterRole.ALLY -> "🤜"
        CharacterRole.TRICKSTER -> "🃏"
    }
}

@Composable
private fun RoleBadge(role: CharacterRole) {
    val color = when (role) {
        CharacterRole.PROTAGONIST -> Color(0xFF4169E1)
        CharacterRole.ANTAGONIST -> Color(0xFF8B0000)
        CharacterRole.MENTOR -> Color(0xFF228B22)
        CharacterRole.LOVE_INTEREST -> Color(0xFFFF69B4)
        CharacterRole.TRICKSTER -> Color(0xFFFFD700)
        else -> MysticPurple
    }
    
    Box(
        modifier = Modifier
            .background(
                color.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = role.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() },
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CharacterQuickStats(character: ExperienceCharacter) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
            StatItem(
                icon = "📖",
                label = "Role",
                value = character.role.name.replace("_", " ").lowercase()
                    .replaceFirstChar { it.uppercase() }
            )
            
            StatItem(
                icon = "⭐",
                label = "Status",
                value = if (character.isMainCharacter) "Main Character" else "Supporting"
            )
            
            StatItem(
                icon = "💫",
                label = "Affinity",
                value = when (character.affinity) {
                    in 0f..0.3f -> "Hostile"
                    in 0.3f..0.5f -> "Neutral"
                    in 0.5f..0.7f -> "Friendly"
                    else -> "Ally"
                }
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: String,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = GildedGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = AncientParchment.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun CharacterPhysicalDescription(description: PhysicalDescription) {
    SectionCard(title = "Physical Appearance", icon = "👤") {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DescriptionRow("Age", description.age.name.lowercase().replaceFirstChar { it.uppercase() })
            DescriptionRow("Height", description.height.name.lowercase().replaceFirstChar { it.uppercase() })
            DescriptionRow("Build", description.build.name.lowercase().replaceFirstChar { it.uppercase() })
            DescriptionRow("Hair", description.hairColor)
            DescriptionRow("Eyes", description.eyeColor)
            
            if (description.distinctiveFeatures.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Distinctive Features",
                    color = GildedGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                description.distinctiveFeatures.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "•",
                            color = MysticPurple,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            color = AncientParchment.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            if (description.clothing.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Typical Attire",
                    color = GildedGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description.clothing,
                    color = AncientParchment.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun DescriptionRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = AncientParchment.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = AncientParchment,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CharacterPersonalitySection(
    traits: List<PersonalityTrait>
) {
    SectionCard(title = "Personality", icon = "💫") {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Traits
            Column {
                Text(
                    text = "Character Traits",
                    color = GildedGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    traits.forEach { trait ->
                        TraitChip(trait = trait.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() }, color = MysticPurple)
                    }
                }
            }
        }
    }
}

@Composable
private fun TraitChip(trait: String, color: Color) {
    Box(
        modifier = Modifier
            .background(
                color.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = trait,
            color = color,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun CharacterBackstory(backstory: String) {
    SectionCard(title = "Backstory", icon = "📜") {
        Text(
            text = backstory,
            color = AncientParchment.copy(alpha = 0.9f),
            fontSize = 15.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
private fun CharacterQuotes(
    quotes: List<CharacterQuote>,
    onQuoteSelected: (CharacterQuote) -> Unit
) {
    SectionCard(title = "Memorable Quotes", icon = "💬") {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            quotes.forEach { quote ->
                QuoteCard(
                    quote = quote,
                    onClick = { onQuoteSelected(quote) }
                )
            }
        }
    }
}

@Composable
private fun QuoteCard(
    quote: CharacterQuote,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Quote mark
            Text(
                text = "❝",
                color = GildedGold.copy(alpha = 0.6f),
                fontSize = 32.sp,
                lineHeight = 0.sp
            )
            
            // Quote text
            Text(
                text = quote.text,
                color = AncientParchment,
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Context
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quote.context,
                    color = AncientParchment.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                
                // Show chapter number
                Box(
                    modifier = Modifier
                        .background(
                            MysticPurple.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Ch. ${quote.chapter}",
                        color = MysticPurple,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterRelationships(relationships: List<CharacterRelationship>) {
    SectionCard(title = "Relationships", icon = "🔗") {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            relationships.forEach { relationship ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    MysticPurple.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = relationship.characterName.first().uppercase(),
                                color = MysticPurple,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = relationship.characterName,
                            color = AncientParchment,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(
                                getRelationshipColor(relationship.relationshipType.name).copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = relationship.relationshipType.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = getRelationshipColor(relationship.relationshipType.name),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

private fun getRelationshipColor(relationship: String): Color {
    return when (relationship.lowercase()) {
        "love", "lover", "partner", "spouse" -> Color(0xFFFF69B4)
        "friend", "ally", "companion" -> Color(0xFF90EE90)
        "enemy", "rival", "nemesis" -> Color(0xFFDC143C)
        "mentor", "teacher", "guide" -> Color(0xFF9370DB)
        "student", "apprentice" -> Color(0xFFADD8E6)
        "family", "sibling", "parent", "child" -> Color(0xFFFFD700)
        else -> AncientParchment
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, GildedGold.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Section header
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GildedGold.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════
 * CHARACTER LIST (Compact View)
 * ═══════════════════════════════════════════════════════════════════
 */

@Composable
fun CharacterList(
    characters: List<ExperienceCharacter>,
    onCharacterSelected: (ExperienceCharacter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(characters) { character ->
            CharacterListItem(
                character = character,
                onClick = { onCharacterSelected(character) }
            )
        }
    }
}

@Composable
private fun CharacterListItem(
    character: ExperienceCharacter,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "charItem")
    
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border"
    )
    
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.8f)
        ),
        border = BorderStroke(
            width = 2.dp,
            color = when (character.role) {
                CharacterRole.PROTAGONIST -> Color(0xFF4169E1).copy(alpha = borderAlpha)
                CharacterRole.ANTAGONIST -> Color(0xFF8B0000).copy(alpha = borderAlpha)
                else -> GildedGold.copy(alpha = borderAlpha)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Portrait
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MysticPurple.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getRoleIcon(character.role),
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Name
            Text(
                text = character.name,
                color = GildedGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Role
            Text(
                text = character.role.name.replace("_", " ").lowercase()
                    .split(" ").first()
                    .replaceFirstChar { it.uppercase() },
                color = AncientParchment.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }
    }
}
