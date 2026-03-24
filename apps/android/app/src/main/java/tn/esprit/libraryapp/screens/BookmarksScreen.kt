package tn.esprit.libraryapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.ui.theme.*
import tn.esprit.libraryapp.viewModel.BookViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════════════
// ✨ ENCHANTED BOOKMARK COLLECTION ✨
// A magical cork board where bookmarks hang as enchanted ribbons
// ═══════════════════════════════════════════════════════════════════

@Composable
fun BookmarksScreen(
    modifier: Modifier = Modifier,
    onBookClick: (Book) -> Unit = {}
) {
    val viewModel: BookViewModel = viewModel()
    val bookmarks by viewModel.bookmarks.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBookmarks()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Mystical Cork Board Background
        MagicalCorkBoardBackground()
        
        // Floating magical dust
        FloatingMagicalDust()
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Enchanted Header with magical bookmark counter
            EnchantedBookmarkHeader(bookmarkCount = bookmarks.size)
            
            if (bookmarks.isEmpty()) {
                // Empty state - awaiting enchanted bookmarks
                EmptyBookmarkSanctuary()
            } else {
                // Staggered bookmark ribbons hanging from the board
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(bookmarks) { index, bookmark ->
                        HangingBookmarkRibbon(
                            book = bookmark,
                            ribbonColor = getEnchantedRibbonColor(index),
                            hangOffset = if (index % 2 == 0) (-20).dp else 20.dp,
                            delay = index * 100,
                            onClick = { onBookClick(bookmark) }
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// MAGICAL CORK BOARD BACKGROUND
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun MagicalCorkBoardBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Cork texture gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3D2914), // Dark wood top
                            Color(0xFF8B6914), // Cork color
                            Color(0xFFA67B5B), // Lighter cork
                            Color(0xFF8B6914), // Cork
                            Color(0xFF3D2914)  // Dark wood bottom
                        )
                    )
                )
        )
        
        // Cork board frame
        Canvas(modifier = Modifier.fillMaxSize()) {
            val frameWidth = 16.dp.toPx()
            
            // Wooden frame - ornate
            drawRect(
                color = RichMahogany,
                topLeft = Offset.Zero,
                size = Size(size.width, frameWidth)
            )
            drawRect(
                color = RichMahogany,
                topLeft = Offset(0f, size.height - frameWidth),
                size = Size(size.width, frameWidth)
            )
            drawRect(
                color = RichMahogany,
                topLeft = Offset.Zero,
                size = Size(frameWidth, size.height)
            )
            drawRect(
                color = RichMahogany,
                topLeft = Offset(size.width - frameWidth, 0f),
                size = Size(frameWidth, size.height)
            )
            
            // Gold inlay on frame
            val inlayOffset = 4.dp.toPx()
            drawRect(
                color = GildedGold.copy(alpha = 0.6f),
                topLeft = Offset(inlayOffset, inlayOffset),
                size = Size(size.width - 2 * inlayOffset, 2.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        
        // Cork texture pattern (dots)
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val dotSpacing = 30.dp.toPx()
            var y = 0f
            while (y < size.height) {
                var x = 0f
                while (x < size.width) {
                    val randomAlpha = Random.nextFloat() * 0.3f
                    drawCircle(
                        color = Color(0xFF5D4E37).copy(alpha = randomAlpha),
                        radius = Random.nextFloat() * 3f + 1f,
                        center = Offset(
                            x + Random.nextFloat() * 10f,
                            y + Random.nextFloat() * 10f
                        )
                    )
                    x += dotSpacing
                }
                y += dotSpacing
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// FLOATING MAGICAL DUST
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun FloatingMagicalDust() {
    val particles = remember {
        List(20) {
            DustParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.3f + 0.1f,
                alpha = Random.nextFloat() * 0.5f + 0.2f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "dust")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dustTime"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val animatedY = (particle.y + time * particle.speed) % 1f
            val shimmer = (sin(time * 10f + particle.x * 20f) + 1f) / 2f
            
            drawCircle(
                color = GildedGold.copy(alpha = particle.alpha * shimmer),
                radius = particle.size,
                center = Offset(
                    particle.x * size.width,
                    animatedY * size.height
                )
            )
        }
    }
}

private data class DustParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)

// ═══════════════════════════════════════════════════════════════════
// ENCHANTED HEADER
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun EnchantedBookmarkHeader(bookmarkCount: Int) {
    val glowAnimation = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowAnimation.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Magical banner background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            DeepLibraryBrown.copy(alpha = 0.9f),
                            MysticPurple.copy(alpha = 0.7f),
                            DeepLibraryBrown.copy(alpha = 0.9f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            GildedGold.copy(alpha = glowAlpha),
                            WarmLeather,
                            GildedGold.copy(alpha = glowAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📜 Enchanted Collection",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AncientParchment
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your saved magical tomes",
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = CandlelightGlow.copy(alpha = 0.8f)
                    )
                }
                
                // Magical bookmark counter orb
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GildedGold.copy(alpha = glowAlpha),
                                    MysticPurple.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$bookmarkCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GildedGold
                        )
                        Text(
                            text = "tomes",
                            fontSize = 10.sp,
                            color = AncientParchment
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// HANGING BOOKMARK RIBBON - The main innovative component
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun HangingBookmarkRibbon(
    book: Book,
    ribbonColor: Color,
    hangOffset: androidx.compose.ui.unit.Dp,
    delay: Int,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        isVisible = true
    }
    
    val swayAnimation = rememberInfiniteTransition(label = "sway")
    val swayAngle by swayAnimation.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000 + delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swayAngle"
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha"
    )
    
    val animatedOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else -50f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "offset"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = hangOffset)
            .graphicsLayer {
                alpha = animatedAlpha
                translationY = animatedOffset
                rotationZ = swayAngle
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
            }
    ) {
        // Pin/tack at top
        BookmarkPin(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-8).dp)
        )
        
        // The ribbon itself
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            // Ribbon top fold
            RibbonTopFold(color = ribbonColor)
            
            // Main ribbon body with book info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ribbonColor,
                                ribbonColor.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = GildedGold.copy(alpha = 0.3f)
                    )
                    .clickable(onClick = onClick)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Book cover in ornate frame
                    OrnateBookCoverFrame(
                        coverUrl = book.coverImage,
                        modifier = Modifier.size(80.dp, 110.dp)
                    )
                    
                    // Book details with magical styling
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = book.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AncientParchment,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "✒",
                                fontSize = 12.sp
                            )
                            Text(
                                text = book.author,
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                color = CandlelightGlow
                            )
                        }
                        
                        // Genre tag as magical seal
                        MagicalGenreSeal(genre = book.genre)
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Pages indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "📖", fontSize = 12.sp)
                                Text(
                                    text = "${book.numOfPages}",
                                    fontSize = 11.sp,
                                    color = AncientParchment.copy(alpha = 0.8f)
                                )
                            }
                            
                            // Year
                            Text(
                                text = "• ${book.publicationYear}",
                                fontSize = 11.sp,
                                color = AncientParchment.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    // Magic action button
                    MagicalReadButton()
                }
            }
            
            // Ribbon bottom tail (V-cut)
            RibbonBottomTail(color = ribbonColor)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// BOOKMARK PIN (decorative tack)
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun BookmarkPin(modifier: Modifier = Modifier) {
    val shimmer = rememberInfiniteTransition(label = "pinShimmer")
    val shimmerAlpha by shimmer.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    
    Box(
        modifier = modifier
            .size(20.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        GildedGold,
                        Color(0xFFB8860B)
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = shimmerAlpha * 0.5f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Pin highlight
        Box(
            modifier = Modifier
                .size(6.dp)
                .offset(x = (-2).dp, y = (-2).dp)
                .background(
                    Color.White.copy(alpha = shimmerAlpha),
                    shape = CircleShape
                )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// RIBBON SHAPE COMPONENTS
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun RibbonTopFold(color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
    ) {
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, size.height * 0.3f)
            lineTo(size.width * 0.5f, 0f)
            lineTo(size.width, size.height * 0.3f)
            lineTo(size.width, size.height)
            close()
        }
        drawPath(path, color.copy(alpha = 0.7f))
    }
}

@Composable
private fun RibbonBottomTail(color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, size.height * 0.7f)
            lineTo(size.width * 0.5f, size.height * 0.4f)
            lineTo(size.width, size.height * 0.7f)
            lineTo(size.width, 0f)
            close()
        }
        drawPath(path, color)
        
        // Shadow/fold line
        drawLine(
            color = DeepLibraryBrown.copy(alpha = 0.3f),
            start = Offset(0f, 0f),
            end = Offset(size.width * 0.5f, size.height * 0.4f),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = DeepLibraryBrown.copy(alpha = 0.3f),
            start = Offset(size.width, 0f),
            end = Offset(size.width * 0.5f, size.height * 0.4f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// ORNATE BOOK COVER FRAME
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun OrnateBookCoverFrame(
    coverUrl: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        GildedGold,
                        Color(0xFFB8860B),
                        GildedGold
                    )
                ),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(3.dp)
    ) {
        AsyncImage(
            model = coverUrl ?: "https://via.placeholder.com/150",
            contentDescription = "Book cover",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp)),
            contentScale = ContentScale.Crop
        )
        
        // Magical shimmer overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            GildedGold.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// MAGICAL GENRE SEAL
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun MagicalGenreSeal(genre: String) {
    Box(
        modifier = Modifier
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MysticPurple.copy(alpha = 0.6f),
                        InkBlue.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = GildedGold.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "✧ $genre ✧",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = GildedGold
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// MAGICAL READ BUTTON
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun MagicalReadButton() {
    val pulseAnimation = rememberInfiniteTransition(label = "pulse")
    val scale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        EnchantedGreen,
                        EnchantedGreen.copy(alpha = 0.7f)
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = GildedGold,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = "Read",
            tint = GildedGold,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// EMPTY STATE - AWAITING ENCHANTED BOOKMARKS
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun EmptyBookmarkSanctuary() {
    val floatAnimation = rememberInfiniteTransition(label = "float")
    val floatOffset by floatAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )
    
    val rotateAnimation by floatAnimation.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotate"
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Floating empty bookmark ribbon
            Box(
                modifier = Modifier
                    .offset(y = floatOffset.dp)
                    .rotate(rotateAnimation)
            ) {
                Canvas(
                    modifier = Modifier.size(80.dp, 120.dp)
                ) {
                    // Empty ribbon shape
                    val path = Path().apply {
                        moveTo(size.width * 0.2f, 0f)
                        lineTo(size.width * 0.8f, 0f)
                        lineTo(size.width * 0.8f, size.height * 0.8f)
                        lineTo(size.width * 0.5f, size.height * 0.6f)
                        lineTo(size.width * 0.2f, size.height * 0.8f)
                        close()
                    }
                    
                    drawPath(
                        path = path,
                        color = MysticPurple.copy(alpha = 0.5f)
                    )
                    drawPath(
                        path = path,
                        color = GildedGold.copy(alpha = 0.6f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                
                // Question mark
                Text(
                    text = "?",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GildedGold.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-10).dp)
                )
            }
            
            Text(
                text = "No Enchanted Bookmarks",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AncientParchment
            )
            
            Text(
                text = "Your magical collection awaits.\nSave a tome to pin it here!",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = CandlelightGlow.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            
            // Magical call to action
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MysticPurple,
                                InkBlue
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = GildedGold,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "✨ Discover Tomes",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GildedGold
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// HELPER FUNCTIONS
// ═══════════════════════════════════════════════════════════════════

private fun getEnchantedRibbonColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF4A2C2A), // Rich mahogany
        Color(0xFF2D1B4E), // Mystic purple
        Color(0xFF1B4332), // Enchanted green
        Color(0xFF1B2838), // Ink blue
        Color(0xFF8B0000).copy(alpha = 0.8f), // Dragons blood
        Color(0xFF4A3728)  // Warm leather brown
    )
    return colors[index % colors.size]
}
