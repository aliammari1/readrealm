package tn.esprit.libraryapp.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.launch
import tn.esprit.libraryapp.R
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.LibrarianAction
import tn.esprit.libraryapp.models.LibrarianAnimations
import tn.esprit.libraryapp.models.LibrarianState
import tn.esprit.libraryapp.models.PickerData
import tn.esprit.libraryapp.models.QuickActionItem
import tn.esprit.libraryapp.models.WheelType

@Composable
fun LibrarianFab(
        state: LibrarianState,
        onAction: (LibrarianAction) -> Unit,
        navController: NavHostController,
        modifier: Modifier = Modifier
) {
    var position by remember { mutableStateOf(Offset.Zero) }
    var isExpanded by remember { mutableStateOf(false) }
    val animations = LibrarianAnimations()

    Box(modifier = modifier.fillMaxSize()) {
        // Radial menu
        RadialMenu(
                isExpanded = isExpanded,
                actions = getQuickActions(),
                onAction = onAction,
                onWheelSelect = { /* Handle wheel selection */}
        )

        // Avatar
        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            LibrarianAvatar(
                    isExpanded = isExpanded,
                    animations = animations,
                    onExpandToggle = { isExpanded = !isExpanded },
                    onAction = onAction
            )

            // Message bubble when visible
            AnimatedVisibility(
                    visible = state.isVisible,
                    enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                    exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End),
                    modifier = Modifier.align(Alignment.CenterStart).padding(end = 80.dp)
            ) { MessageBubble(text = state.message, isTyping = state.isTyping) }
        }
    }
}

private fun getQuickActions() =
        listOf(
                QuickActionItem(
                        icon = Icons.Default.Category,
                        label = "Browse Genres",
                        wheelType = WheelType.GENRE
                ),
                QuickActionItem(
                        icon = Icons.Default.Translate,
                        label = "Translate",
                        wheelType = WheelType.LANGUAGE
                ),
                QuickActionItem(
                        icon = Icons.Default.Sort,
                        label = "Sort Books",
                        wheelType = WheelType.SORT
                ),
                QuickActionItem(
                        icon = Icons.Default.Camera,
                        label = "Scan Book",
                        action = LibrarianAction.Chat("scan a book")
                ),
                QuickActionItem(
                        icon = Icons.Default.Mic,
                        label = "Voice Search",
                        action = LibrarianAction.Chat("voice search")
                )
        )

private fun getWheelOptions(type: WheelType) =
        when (type) {
            WheelType.GENRE -> Genre.entries.map { genre -> genre.value }
            WheelType.LANGUAGE ->
                    listOf("English", "French", "Spanish", "German", "Italian", "Arabic")
            WheelType.SORT ->
                    listOf(
                            "Relevance",
                            "Title (A-Z)",
                            "Title (Z-A)",
                            "Author (A-Z)",
                            "Author (Z-A)",
                            "Newest First",
                            "Oldest First"
                    )
            WheelType.ACTION -> listOf("Search", "Scan", "Voice", "Translate", "Recommend")
        }

private fun handleWheelSelection(
        type: WheelType,
        selection: String,
        onAction: (LibrarianAction) -> Unit
) {
    val action =
            when (type) {
                WheelType.GENRE -> LibrarianAction.Recommend(selection)
                WheelType.LANGUAGE -> LibrarianAction.TranslateText("", selection)
                WheelType.SORT -> LibrarianAction.Search(selection)
                WheelType.ACTION ->
                        when (selection) {
                            "Search" -> LibrarianAction.Chat("search")
                            "Scan" -> LibrarianAction.Chat("scan")
                            "Voice" -> LibrarianAction.Chat("voice")
                            "Translate" -> LibrarianAction.Chat("translate")
                            "Recommend" -> LibrarianAction.Chat("recommend")
                            else -> null
                        }
            }
    action?.let { onAction(it) }
}

enum class WheelPickerType {
    None,
    Genre,
    Sort,
    Language
}

@Composable
private fun LibrarianMessage(text: String, isTyping: Boolean, modifier: Modifier = Modifier) {
    Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            if (isTyping) {
                TypingIndicator()
            } else {
                Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "")
            val scale by
                    infiniteTransition.animateFloat(
                            initialValue = 0.5f,
                            targetValue = 1f,
                            animationSpec =
                                    infiniteRepeatable(
                                            animation = tween(600, delayMillis = index * 200),
                                            repeatMode = RepeatMode.Reverse
                                    ),
                            label = "typing"
                    )

            Box(
                    modifier =
                            Modifier.size(8.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

// Update LibrarianAvatar to include spring-based movement
@Composable
private fun LibrarianAvatar(
        isExpanded: Boolean,
        animations: LibrarianAnimations,
        position: Offset = Offset.Zero,
        onPositionChange: (Offset) -> Unit = {},
        onExpandToggle: () -> Unit,
        onAction: (LibrarianAction) -> Unit,
        modifier: Modifier = Modifier,
        onShowPicker: (PickerData) -> Unit = {} // Add this parameter
) {
    val springSpec = remember {
        SpringSpec<Float>(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
        )
    }

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(position) {
        launch { offsetX.animateTo(position.x, springSpec) }
        launch { offsetY.animateTo(position.y, springSpec) }
    }

    val avatarSize = 64.dp
    val colorScheme = MaterialTheme.colorScheme
    val avatarScale = remember { Animatable(1f) }
    val glowRadius = remember { Animatable(1f) }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            launch { avatarScale.animateTo(targetValue = 1.1f, animationSpec = springSpec) }
            launch { glowRadius.animateTo(targetValue = 1.4f, animationSpec = springSpec) }
        } else {
            launch { avatarScale.animateTo(targetValue = 1f, animationSpec = springSpec) }
            launch { glowRadius.animateTo(targetValue = 1f, animationSpec = springSpec) }
        }
    }

    Box(modifier = modifier) {
        // Avatar content with proper z-index
        Box(
                modifier =
                        Modifier.offset {
                                    IntOffset(
                                            x = offsetX.value.roundToInt(),
                                            y = offsetY.value.roundToInt()
                                    )
                                }
                                .zIndex(1f) // Ensure avatar stays on top
        ) {
            // Enhanced glow effect
            Box(
                    modifier =
                            Modifier.size(avatarSize * glowRadius.value)
                                    .align(Alignment.Center)
                                    .graphicsLayer {
                                        alpha = animations.glowAnim
                                        scaleX = avatarScale.value
                                        scaleY = avatarScale.value
                                    }
                                    .background(
                                            brush =
                                                    Brush.radialGradient(
                                                            colors =
                                                                    listOf(
                                                                            colorScheme.primary
                                                                                    .copy(
                                                                                            alpha =
                                                                                                    0.3f
                                                                                    ),
                                                                            colorScheme.primary
                                                                                    .copy(
                                                                                            alpha =
                                                                                                    0.1f
                                                                                    ),
                                                                            Color.Transparent
                                                                    )
                                                    ),
                                            shape = CircleShape
                                    )
            )

            // Enhanced avatar surface with ripple effect
            Surface(
                    onClick = {
                        onExpandToggle()
                        if (isExpanded) onAction(LibrarianAction.Welcome)
                    },
                    modifier =
                            Modifier.size(avatarSize)
                                    .graphicsLayer {
                                        scaleX = avatarScale.value
                                        scaleY = avatarScale.value
                                    }
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            onPositionChange(
                                                    Offset(
                                                            x = position.x + dragAmount.x,
                                                            y = position.y + dragAmount.y
                                                    )
                                            )
                                        }
                                    },
                    shape = CircleShape,
                    color = colorScheme.primaryContainer,
                    border =
                            BorderStroke(
                                    2.dp,
                                    Brush.sweepGradient(
                                            listOf(
                                                    colorScheme.primary.copy(
                                                            alpha = animations.glowAnim
                                                    ),
                                                    colorScheme.tertiary,
                                                    colorScheme.primary.copy(
                                                            alpha = animations.glowAnim
                                                    )
                                            )
                                    )
                            ),
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp
            ) {
                Image(
                        painter = painterResource(R.drawable.librarian_avatar),
                        contentDescription = "AI Assistant",
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .graphicsLayer {
                                            rotationZ = sin(animations.floatAnim) * 2f
                                        }
                )
            }
        }
    }
}

// Add new radial menu composable
@Composable
private fun RadialMenu(
        isExpanded: Boolean,
        actions: List<QuickActionItem>,
        onAction: (LibrarianAction) -> Unit,
        onWheelSelect: (WheelType) -> Unit
) {
    val transition = updateTransition(isExpanded, label = "radial")
    val scale by
            transition.animateFloat(
                    transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
                    label = "scale"
            ) { if (it) 1f else 0f }

    val rotation by
            transition.animateFloat(
                    transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
                    label = "rotation"
            ) { if (it) 360f else 0f }

    if (isExpanded) {
        Box(modifier = Modifier.fillMaxSize()) {
            actions.forEachIndexed { index, action ->
                val angle = 360f / actions.size * index
                val offset = 120.dp
                val x = (offset.value.toInt() * kotlin.math.cos(Math.toRadians(angle.toDouble())))
                val y = (offset.value.toInt() * kotlin.math.sin(Math.toRadians(angle.toDouble())))

                ActionButton(
                        action = action,
                        modifier =
                                Modifier.offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                                        .align(Alignment.Center)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            rotationZ = rotation
                                            alpha = scale
                                        },
                        onClick = {
                            when {
                                action.wheelType != null -> onWheelSelect(action.wheelType)
                                action.action != null -> onAction(action.action)
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
        action: QuickActionItem,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
            onClick = onClick,
            modifier = modifier.size(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            border =
                    BorderStroke(
                            1.dp,
                            Brush.sweepGradient(
                                    listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                    )
                            )
                    ),
            tonalElevation = if (isPressed) 0.dp else 8.dp,
            shadowElevation = if (isPressed) 4.dp else 12.dp
    ) {
        Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            Icon(
                    imageVector = action.icon,
                    contentDescription = action.label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
            )
            Text(
                    text = action.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun MessageBubble(text: String, isTyping: Boolean, modifier: Modifier = Modifier) {
    Surface(
            modifier = modifier.widthIn(max = 256.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            tonalElevation = 4.dp
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            if (isTyping) {
                TypingIndicator()
            } else {
                Text(text = text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

