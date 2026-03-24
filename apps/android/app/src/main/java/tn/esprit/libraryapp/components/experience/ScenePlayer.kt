package tn.esprit.libraryapp.components.experience

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlinx.coroutines.delay
import tn.esprit.libraryapp.models.experience.*
import tn.esprit.libraryapp.ui.theme.*
import kotlin.math.*

/**
 * ═══════════════════════════════════════════════════════════════════
 * SCENE PLAYER
 * Immersive scene playback with dialogue, narration, and effects
 * ═══════════════════════════════════════════════════════════════════
 */

@Composable
fun ScenePlayer(
    scene: ExperienceScene,
    characters: List<ExperienceCharacter>,
    onDialogueComplete: () -> Unit,
    onSceneComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentDialogueIndex by remember { mutableStateOf(0) }
    var isDialogueVisible by remember { mutableStateOf(false) }
    var displayedText by remember { mutableStateOf("") }
    var isAutoPlaying by remember { mutableStateOf(false) }
    
    val currentDialogue = scene.dialogues.getOrNull(currentDialogueIndex)
    
    // Typewriter effect for dialogue
    LaunchedEffect(currentDialogue, isDialogueVisible) {
        if (currentDialogue != null && isDialogueVisible) {
            displayedText = ""
            currentDialogue.text.forEachIndexed { index, char ->
                delay(if (char == '.' || char == ',' || char == '!' || char == '?') 150L else 30L)
                displayedText = currentDialogue.text.take(index + 1)
            }
        }
    }
    
    // Calculate display duration from SceneDuration enum
    val dialogueDuration = when (scene.duration) {
        SceneDuration.BRIEF -> 2000L
        SceneDuration.SHORT -> 4000L
        SceneDuration.MEDIUM -> 6000L
        SceneDuration.LONG -> 10000L
        SceneDuration.EXTENDED -> 15000L
    }
    
    // Auto-play functionality
    LaunchedEffect(isAutoPlaying, currentDialogueIndex) {
        if (isAutoPlaying && currentDialogue != null) {
            delay(dialogueDuration)
            if (currentDialogueIndex < scene.dialogues.size - 1) {
                currentDialogueIndex++
            } else {
                onSceneComplete()
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Scene background based on mood
        SceneBackgroundEffect(mood = scene.mood)
        
        // Visual effects can be derived from key events
        scene.keyEvents.mapNotNull { it.visualEffect }.forEach { effect ->
            VisualEffectRenderer(effect = effect)
        }
        
        // Character portraits (if present in dialogue)
        currentDialogue?.let { dialogue ->
            val speaker = characters.find { it.id == dialogue.speakerId }
            speaker?.let {
                CharacterPortrait(
                    character = it,
                    emotion = dialogue.emotion,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(24.dp)
                )
            }
        }
        
        // Dialogue box
        AnimatedVisibility(
            visible = isDialogueVisible && currentDialogue != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            currentDialogue?.let { dialogue ->
                DialogueBox(
                    dialogue = dialogue,
                    displayedText = displayedText,
                    character = characters.find { it.id == dialogue.speakerId },
                    isComplete = displayedText == dialogue.text,
                    onAdvance = {
                        if (displayedText != dialogue.text) {
                            displayedText = dialogue.text
                        } else if (currentDialogueIndex < scene.dialogues.size - 1) {
                            currentDialogueIndex++
                            onDialogueComplete()
                        } else {
                            onSceneComplete()
                        }
                    }
                )
            }
        }
        
        // Scene title overlay (shown briefly at start)
        var showTitle by remember { mutableStateOf(true) }
        
        LaunchedEffect(Unit) {
            delay(3000)
            showTitle = false
            isDialogueVisible = true
        }
        
        AnimatedVisibility(
            visible = showTitle,
            enter = fadeIn(animationSpec = tween(1000)),
            exit = fadeOut(animationSpec = tween(1000)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            SceneTitleCard(
                title = scene.sceneTitle,
                chapterReference = "Chapter ${scene.chapterNumber}: ${scene.chapterTitle}"
            )
        }
        
        // Scene controls
        SceneControls(
            isAutoPlaying = isAutoPlaying,
            onToggleAutoPlay = { isAutoPlaying = !isAutoPlaying },
            onSkip = onSceneComplete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
        
        // Scene progress indicator
        SceneProgressIndicator(
            currentIndex = currentDialogueIndex,
            totalDialogues = scene.dialogues.size,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
    }
}

@Composable
private fun SceneBackgroundEffect(mood: SceneMood) {
    val infiniteTransition = rememberInfiniteTransition(label = "sceneBg")
    
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorShift"
    )
    
    val colors = when (mood) {
        SceneMood.TENSE -> listOf(
            Color(0xFF1A0A0A),
            Color(0xFF2D0A0A),
            Color(0xFF1A0A0A)
        )
        SceneMood.ROMANTIC -> listOf(
            Color(0xFF2D0A1A),
            Color(0xFF4A0A2A),
            Color(0xFF2D0A1A)
        )
        SceneMood.MYSTERIOUS -> listOf(
            Color(0xFF0A0A2D),
            Color(0xFF1A0A4A),
            Color(0xFF0A0A2D)
        )
        SceneMood.JOYFUL -> listOf(
            Color(0xFF1A2D0A),
            Color(0xFF2A4A0A),
            Color(0xFF1A2D0A)
        )
        SceneMood.MELANCHOLIC -> listOf(
            Color(0xFF0A1A2D),
            Color(0xFF0A2A4A),
            Color(0xFF0A1A2D)
        )
        SceneMood.EPIC -> listOf(
            Color(0xFF2D1A0A),
            Color(0xFF4A2A0A),
            Color(0xFF2D1A0A)
        )
        SceneMood.TERRIFYING -> listOf(
            Color(0xFF0A0A0A),
            Color(0xFF1A0A0A),
            Color(0xFF0A0A0A)
        )
        else -> listOf(
            Color(0xFF1A1A2D),
            Color(0xFF2A2A4A),
            Color(0xFF1A1A2D)
        )
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Animated gradient background
        val gradientCenter = Offset(
            size.width * (0.3f + colorShift * 0.4f),
            size.height * (0.3f + colorShift * 0.4f)
        )
        
        drawRect(
            brush = Brush.radialGradient(
                colors = colors,
                center = gradientCenter,
                radius = size.maxDimension
            )
        )
        
        // Particle effects based on mood
        when (mood) {
            SceneMood.ROMANTIC -> {
                // Floating hearts/petals
                repeat(20) { i ->
                    val x = size.width * ((i * 37 + colorShift * 100) % 100) / 100
                    val y = size.height * ((i * 53 + colorShift * 50) % 100) / 100
                    
                    drawCircle(
                        color = Color(0x40FF69B4),
                        radius = 3f + (i % 3),
                        center = Offset(x, y)
                    )
                }
            }
            SceneMood.MYSTERIOUS -> {
                // Ethereal wisps
                repeat(15) { i ->
                    val angle = (colorShift * 360 + i * 24) * PI / 180
                    val radius = 100f + i * 20
                    val x = size.width / 2 + cos(angle).toFloat() * radius
                    val y = size.height / 2 + sin(angle).toFloat() * radius
                    
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x409370DB),
                                Color.Transparent
                            ),
                            center = Offset(x, y),
                            radius = 30f
                        ),
                        radius = 30f,
                        center = Offset(x, y)
                    )
                }
            }
            SceneMood.EPIC -> {
                // Sparks/embers
                repeat(25) { i ->
                    val x = size.width * ((i * 41) % 100) / 100
                    val baseY = size.height
                    val floatY = baseY - (colorShift * size.height + i * 30) % size.height
                    
                    drawCircle(
                        color = Color(0x60FF8C00),
                        radius = 2f + (i % 3),
                        center = Offset(x, floatY)
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun DialogueBox(
    dialogue: SceneDialogue,
    displayedText: String,
    character: ExperienceCharacter?,
    isComplete: Boolean,
    onAdvance: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAdvance() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Speaker name
            character?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = getEmotionColor(dialogue.emotion),
                                shape = CircleShape
                            )
                    )
                    
                    Text(
                        text = it.name,
                        color = GildedGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    
                    Text(
                        text = "(${dialogue.emotion.name.lowercase().replaceFirstChar { c -> c.uppercase() }})",
                        color = getEmotionColor(dialogue.emotion).copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            } ?: run {
                // Narration
                Text(
                    text = "✦ Narration ✦",
                    color = AncientParchment.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Dialogue text with typewriter effect
            Text(
                text = displayedText,
                color = AncientParchment,
                fontSize = 18.sp,
                lineHeight = 28.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Continue indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (isComplete) {
                    val infiniteTransition = rememberInfiniteTransition(label = "continue")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "blink"
                    )
                    
                    Text(
                        text = "Tap to continue ▶",
                        color = GildedGold.copy(alpha = alpha),
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "Tap to skip ⏩",
                        color = AncientParchment.copy(alpha = 0.4f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun getEmotionColor(emotion: DialogueEmotion): Color {
    return when (emotion) {
        DialogueEmotion.NEUTRAL -> AncientParchment
        DialogueEmotion.HAPPY -> Color(0xFF90EE90)
        DialogueEmotion.SAD -> Color(0xFF6495ED)
        DialogueEmotion.ANGRY -> Color(0xFFDC143C)
        DialogueEmotion.FEARFUL -> Color(0xFF9370DB)
        DialogueEmotion.SURPRISED -> Color(0xFFFFD700)
        DialogueEmotion.LOVING -> Color(0xFFFF69B4)
        DialogueEmotion.DETERMINED -> Color(0xFFFF8C00)
        DialogueEmotion.DISGUSTED -> Color(0xFF556B2F)
        DialogueEmotion.SUSPICIOUS -> Color(0xFF708090)
        DialogueEmotion.DEFEATED -> Color(0xFF696969)
        DialogueEmotion.SARCASTIC -> Color(0xFFDAA520)
        DialogueEmotion.WISE -> Color(0xFF8FBC8F)
        DialogueEmotion.MENACING -> Color(0xFF8B0000)
    }
}

@Composable
private fun CharacterPortrait(
    character: ExperienceCharacter,
    emotion: DialogueEmotion,
    modifier: Modifier = Modifier
) {
    val emotionColor = getEmotionColor(emotion)
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Portrait frame
        Box(
            modifier = Modifier
                .size(120.dp)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(emotionColor, GildedGold, emotionColor)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(3.dp)
        ) {
            // Character silhouette/placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                DeepLibraryBrown,
                                DeepLibraryBrown.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(9.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Character initial or emoji based on role
                val icon = when (character.role) {
                    CharacterRole.PROTAGONIST -> "⚔️"
                    CharacterRole.MENTOR -> "🧙"
                    CharacterRole.ANTAGONIST -> "😈"
                    CharacterRole.SIDEKICK -> "🤝"
                    CharacterRole.LOVE_INTEREST -> "💕"
                    CharacterRole.TRICKSTER -> "🎭"
                    CharacterRole.GUARDIAN -> "🛡️"
                    CharacterRole.HERALD -> "📯"
                    CharacterRole.SHAPESHIFTER -> "🌙"
                    CharacterRole.SHADOW -> "👤"
                    CharacterRole.ALLY -> "🤜"
                    CharacterRole.DEUTERAGONIST -> "🌟"
                    CharacterRole.COMIC_RELIEF -> "🃏"
                }
                
                Text(
                    text = icon,
                    fontSize = 48.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Character name
        Text(
            text = character.name,
            color = GildedGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )
        
        // Role badge
        Text(
            text = character.role.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() },
            color = AncientParchment.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun SceneTitleCard(
    title: String,
    chapterReference: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DeepLibraryBrown.copy(alpha = 0.9f),
                        Color.Transparent
                    ),
                    radius = 300f
                )
            )
            .padding(48.dp)
    ) {
        Text(
            text = "✦ ✦ ✦",
            color = GildedGold,
            fontSize = 24.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            color = GildedGold,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = chapterReference,
            color = AncientParchment.copy(alpha = 0.7f),
            fontSize = 16.sp,
            fontStyle = FontStyle.Italic
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "✦ ✦ ✦",
            color = GildedGold,
            fontSize = 24.sp
        )
    }
}

@Composable
private fun SceneControls(
    isAutoPlaying: Boolean,
    onToggleAutoPlay: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Auto-play toggle
        IconButton(
            onClick = onToggleAutoPlay,
            modifier = Modifier
                .background(
                    DeepLibraryBrown.copy(alpha = 0.8f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = if (isAutoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isAutoPlaying) "Pause auto-play" else "Start auto-play",
                tint = GildedGold
            )
        }
        
        // Skip scene
        IconButton(
            onClick = onSkip,
            modifier = Modifier
                .background(
                    DeepLibraryBrown.copy(alpha = 0.8f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Skip scene",
                tint = AncientParchment.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SceneProgressIndicator(
    currentIndex: Int,
    totalDialogues: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(totalDialogues.coerceAtMost(10)) { index ->
            val isCurrent = index == currentIndex
            val isPast = index < currentIndex
            
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(if (isCurrent) 10.dp else 6.dp)
                    .background(
                        color = when {
                            isCurrent -> GildedGold
                            isPast -> MysticPurple.copy(alpha = 0.6f)
                            else -> AncientParchment.copy(alpha = 0.3f)
                        },
                        shape = CircleShape
                    )
            )
        }
        
        if (totalDialogues > 10) {
            Text(
                text = "+${totalDialogues - 10}",
                color = AncientParchment.copy(alpha = 0.5f),
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun VisualEffectRenderer(effect: VisualEffect) {
    when (effect.type) {
        EffectType.GLOW -> EtherealGlowEffect()
        EffectType.SPARKLE -> ParticleBurstEffect()
        EffectType.LIGHTNING -> FlashEffect()
        EffectType.FIRE -> ColorTintOverlay(Color(0xFFFF4500), effect.intensity)
        EffectType.SMOKE -> BlurOverlay(effect.intensity)
        EffectType.MIST -> VignetteEffect(effect.intensity)
        EffectType.PORTAL -> EtherealGlowEffect()
        EffectType.EXPLOSION -> FlashEffect()
        EffectType.MAGIC_CIRCLE -> EtherealGlowEffect()
        EffectType.TRANSFORMATION -> FadeOverlay(Color.White, effect.intensity)
        EffectType.FADE -> FadeOverlay(Color.Black, effect.intensity)
        EffectType.SHAKE -> ScreenShakeEffect()
    }
}

@Composable
private fun ScreenShakeEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeX"
    )
    
    // This is a placeholder - actual shake would be applied to parent
}

@Composable
private fun FadeOverlay(color: Color, intensity: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.copy(alpha = intensity))
    )
}

@Composable
private fun FlashEffect() {
    var showFlash by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(100)
        showFlash = false
    }
    
    AnimatedVisibility(
        visible = showFlash,
        enter = fadeIn(tween(50)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        )
    }
}

@Composable
private fun BlurOverlay(intensity: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = intensity * 0.3f))
            .blur(radius = (intensity * 10).dp)
    )
}

@Composable
private fun VignetteEffect(intensity: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = intensity)
                ),
                center = Offset(size.width / 2, size.height / 2),
                radius = size.maxDimension * 0.7f
            )
        )
    }
}

@Composable
private fun ColorTintOverlay(color: Color, intensity: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.copy(alpha = intensity * 0.3f))
    )
}

@Composable
private fun ParticleBurstEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "burst")
    
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "burstProgress"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        repeat(20) { i ->
            val angle = i * (360f / 20) * PI / 180
            val distance = progress * size.minDimension / 2
            
            val x = centerX + cos(angle).toFloat() * distance
            val y = centerY + sin(angle).toFloat() * distance
            
            drawCircle(
                color = GildedGold.copy(alpha = 1f - progress),
                radius = 4f * (1f - progress * 0.5f),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun SlowMotionIndicator() {
    // Visual indicator for slow motion
}

@Composable
private fun ZoomIndicator(intensity: Float) {
    // Visual indicator for zoom
}

@Composable
private fun PanIndicator() {
    // Visual indicator for pan
}

@Composable
private fun LensFlareEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "flare")
    
    val flareAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flareAlpha"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val flareX = size.width * 0.7f
        val flareY = size.height * 0.2f
        
        // Main flare
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = flareAlpha),
                    Color.Yellow.copy(alpha = flareAlpha * 0.5f),
                    Color.Transparent
                ),
                center = Offset(flareX, flareY),
                radius = 100f
            ),
            radius = 100f,
            center = Offset(flareX, flareY)
        )
        
        // Secondary flares
        listOf(0.3f, 0.5f, 0.7f).forEach { ratio ->
            val x = flareX - (flareX - size.width / 2) * ratio * 2
            val y = flareY + (size.height / 2 - flareY) * ratio * 2
            
            drawCircle(
                color = Color(0x20FFD700),
                radius = 20f + ratio * 30,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun RainDropsOnLens() {
    val infiniteTransition = rememberInfiniteTransition(label = "rainDrops")
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainTime"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        repeat(15) { i ->
            val x = size.width * ((i * 29) % 100) / 100
            val startY = -50f + (i * 23) % 100
            val y = startY + time * (size.height + 100)
            
            if (y > 0 && y < size.height) {
                // Raindrop streak
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(x, y),
                    end = Offset(x - 5, y + 30),
                    strokeWidth = 2f
                )
            }
        }
    }
}

@Composable
private fun DustParticlesEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "dust")
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dustTime"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        repeat(30) { i ->
            val x = (size.width * ((i * 37 + time * 100) % 100) / 100)
            val y = size.height * ((i * 53) % 100) / 100
            val alpha = (sin((time * PI * 2 + i).toFloat()) + 1) / 2 * 0.4f
            
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 1.5f + (i % 2),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun EtherealGlowEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "ethereal")
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "etherealPulse"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    MysticPurple.copy(alpha = pulse * 0.2f),
                    Color.Transparent
                ),
                center = Offset(size.width / 2, size.height / 2),
                radius = size.maxDimension * 0.8f
            )
        )
    }
}
