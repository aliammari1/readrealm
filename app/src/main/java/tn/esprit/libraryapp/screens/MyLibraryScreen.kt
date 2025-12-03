package tn.esprit.libraryapp.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
// ✨ ENCHANTED PERSONAL LIBRARY SANCTUARY ✨
// A magical library with floating bookshelves and mystical orbs
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLibraryScreen(modifier: Modifier = Modifier, onBookClick: (Book) -> Unit = {}) {
    val viewModel: BookViewModel = viewModel()
    val bookmarks by viewModel.bookmarks.collectAsState()
    var selectedCategory by remember { mutableStateOf("All Tomes") }
    val categories = listOf("All Tomes", "Currently Reading", "Completed", "Want to Read")

    LaunchedEffect(Unit) { viewModel.loadBookmarks() }

    Box(modifier = modifier.fillMaxSize()) {
        // Mystical library background
        LibrarySanctuaryBackground()
        
        // Floating magical particles
        FloatingLibraryParticles()
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Enchanted library header with stats
            EnchantedLibraryHeader(
                totalBooks = bookmarks.size
            )
            
            // Reading statistics crystal orbs
            ReadingStatsCrystals(books = bookmarks)
            
            // Category selection as magical runes
            MagicalCategoryRunes(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (bookmarks.isEmpty()) {
                EmptyLibrarySanctuary()
            } else {
                // Floating bookshelf display
                FloatingBookshelfDisplay(
                    books = bookmarks,
                    onBookClick = onBookClick
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// LIBRARY SANCTUARY BACKGROUND
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun LibrarySanctuaryBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Deep mystical gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D0B14), // Deep night
                            Color(0xFF1A0F1F), // Dark purple
                            Color(0xFF1A1520), // Purple-brown
                            Color(0xFF0D0B0A)  // Deep black-brown
                        )
                    )
                )
        )
        
        // Starfield effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(50) {
                val x = Random.nextFloat() * size.width
                val y = Random.nextFloat() * size.height
                val starSize = Random.nextFloat() * 2f + 0.5f
                val alpha = Random.nextFloat() * 0.6f + 0.2f
                
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = starSize,
                    center = Offset(x, y)
                )
            }
        }
        
        // Mystical fog at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MysticPurple.copy(alpha = 0.1f),
                            DeepLibraryBrown.copy(alpha = 0.3f)
                        )
                    )
                )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// FLOATING LIBRARY PARTICLES
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun FloatingLibraryParticles() {
    val particles = remember {
        List(15) {
            LibraryParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 6f + 3f,
                speed = Random.nextFloat() * 0.2f + 0.05f,
                symbol = listOf("📚", "✨", "📖", "🌟", "📜").random()
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleTime"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        particles.forEachIndexed { index, particle ->
            val yOffset = ((particle.y + time * particle.speed) % 1f)
            val xWobble = sin(time * 3f + index) * 10f
            
            Text(
                text = particle.symbol,
                fontSize = particle.size.sp,
                modifier = Modifier
                    .offset(
                        x = (particle.x * 300f + xWobble).dp,
                        y = (yOffset * 800f).dp
                    )
                    .graphicsLayer {
                        alpha = 0.4f + sin(time * 5f + index) * 0.2f
                    }
            )
        }
    }
}

private data class LibraryParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val symbol: String
)

// ═══════════════════════════════════════════════════════════════════
// ENCHANTED LIBRARY HEADER
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun EnchantedLibraryHeader(totalBooks: Int) {
    val glowAnimation = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowAnimation.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title with magical decoration
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Magical book icon
                    Box(
                        modifier = Modifier
                            .size(50.dp)
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
                        Text(
                            text = "📚",
                            fontSize = 24.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = "My Sanctuary",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = AncientParchment
                        )
                        Text(
                            text = "Personal enchanted collection",
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            color = CandlelightGlow.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Total books crystal counter
            Box(
                modifier = Modifier
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MysticPurple,
                                MysticPurple.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = GildedGold.copy(alpha = glowAlpha),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalBooks",
                        fontSize = 24.sp,
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

// ═══════════════════════════════════════════════════════════════════
// READING STATS CRYSTALS - Floating crystal orbs showing stats
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun ReadingStatsCrystals(books: List<Book>) {
    val totalPages = books.sumOf { it.numOfPages }
    val avgRating = if (books.isNotEmpty()) books.map { it.averageRating } else 0.0
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCrystalOrb(
            icon = "⏱",
            value = "${books.size * 3}h",
            label = "Read Time",
            crystalColor = EnchantedGreen
        )
        StatCrystalOrb(
            icon = "📖",
            value = "$totalPages",
            label = "Total Pages",
            crystalColor = MysticPurple
        )
        StatCrystalOrb(
            icon = "⭐",
            value = "0.0",
            label = "Avg Rating",
            crystalColor = GildedGold
        )
    }
}

@Composable
private fun StatCrystalOrb(
    icon: String,
    value: String,
    label: String,
    crystalColor: Color
) {
    val floatAnimation = rememberInfiniteTransition(label = "float")
    val floatOffset by floatAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )
    
    val glowAlpha by floatAnimation.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Column(
        modifier = Modifier.offset(y = floatOffset.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Crystal orb
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            crystalColor.copy(alpha = glowAlpha),
                            crystalColor.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            crystalColor,
                            crystalColor.copy(alpha = 0.5f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = icon,
                    fontSize = 16.sp
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AncientParchment
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 10.sp,
            color = CandlelightGlow.copy(alpha = 0.8f)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// MAGICAL CATEGORY RUNES
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun MagicalCategoryRunes(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            CategoryRuneButton(
                category = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelect(category) }
            )
        }
    }
}

@Composable
private fun CategoryRuneButton(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val glowAnimation = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowAnimation.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val runeIcon = when (category) {
        "All Tomes" -> "📚"
        "Currently Reading" -> "📖"
        "Completed" -> "✅"
        "Want to Read" -> "📝"
        else -> "📜"
    }
    
    Box(
        modifier = Modifier
            .scale(scale)
            .background(
                brush = if (isSelected) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            MysticPurple,
                            InkBlue
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            DeepLibraryBrown.copy(alpha = 0.7f),
                            RichMahogany.copy(alpha = 0.7f)
                        )
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = GildedGold.copy(alpha = glowAlpha),
                        shape = RoundedCornerShape(20.dp)
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = WarmLeather.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = runeIcon,
                fontSize = 14.sp
            )
            Text(
                text = category,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) GildedGold else AncientParchment
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// FLOATING BOOKSHELF DISPLAY
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun FloatingBookshelfDisplay(
    books: List<Book>,
    onBookClick: (Book) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Display books in rows of 3 on floating shelves
        val chunkedBooks = books.chunked(3)
        
        itemsIndexed(chunkedBooks) { shelfIndex, shelfBooks ->
            FloatingBookshelf(
                books = shelfBooks,
                shelfIndex = shelfIndex,
                onBookClick = onBookClick
            )
        }
    }
}

@Composable
private fun FloatingBookshelf(
    books: List<Book>,
    shelfIndex: Int,
    onBookClick: (Book) -> Unit
) {
    val floatAnimation = rememberInfiniteTransition(label = "shelf")
    val floatOffset by floatAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000 + shelfIndex * 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shelfFloat"
    )
    
    Column(
        modifier = Modifier
            .offset(y = floatOffset.dp)
            .fillMaxWidth()
    ) {
        // Books on shelf
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            books.forEachIndexed { index, book ->
                MagicalBookOnShelf(
                    book = book,
                    delay = index * 200,
                    onClick = { onBookClick(book) }
                )
            }
            
            // Fill empty spots with placeholder
            repeat(3 - books.size) {
                Box(modifier = Modifier.width(100.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Floating wooden shelf
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            WarmLeather,
                            RichMahogany,
                            DeepLibraryBrown
                        )
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                .border(
                    width = 1.dp,
                    color = GildedGold.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            // Shelf edge highlight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                GildedGold.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        
        // Shelf shadow/glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MysticPurple.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// MAGICAL BOOK ON SHELF
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun MagicalBookOnShelf(
    book: Book,
    delay: Int,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        isVisible = true
    }
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha"
    )
    
    val hoverAnimation = rememberInfiniteTransition(label = "hover")
    val glowPulse by hoverAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    
    Column(
        modifier = Modifier
            .width(100.dp)
            .graphicsLayer { alpha = animatedAlpha }
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Book with magical glow
        Box {
            // Glow behind book
            Box(
                modifier = Modifier
                    .size(90.dp, 130.dp)
                    .offset(y = 5.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MysticPurple.copy(alpha = glowPulse),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
            
            // Book cover
            Box(
                modifier = Modifier
                    .size(85.dp, 120.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                GildedGold.copy(alpha = 0.8f),
                                WarmLeather,
                                GildedGold.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(3.dp)
            ) {
                AsyncImage(
                    model = book.coverImage ?: "https://via.placeholder.com/150",
                    contentDescription = "Book cover",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(2.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Magical overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MysticPurple.copy(alpha = 0.2f)
                                )
                            )
                        )
                )
            }
            
            // Spine highlight
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(120.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                GildedGold.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Book title
        Text(
            text = book.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = AncientParchment,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
        
        // Author
        Text(
            text = book.author,
            fontSize = 9.sp,
            fontStyle = FontStyle.Italic,
            color = CandlelightGlow.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// EMPTY LIBRARY SANCTUARY
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun EmptyLibrarySanctuary() {
    val floatAnimation = rememberInfiniteTransition(label = "emptyFloat")
    val floatOffset by floatAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emptyFloatOffset"
    )
    
    val rotateAnim by floatAnimation.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emptyRotate"
    )
    
    val glowAlpha by floatAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emptyGlow"
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Floating empty bookshelf
            Box(
                modifier = Modifier
                    .offset(y = floatOffset.dp)
                    .rotate(rotateAnim)
            ) {
                // Magical glow
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MysticPurple.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                // Empty shelf icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Center)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    WarmLeather,
                                    RichMahogany
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = GildedGold.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📚",
                        fontSize = 40.sp,
                        modifier = Modifier.graphicsLayer { alpha = 0.3f }
                    )
                }
            }
            
            Text(
                text = "Your Sanctuary Awaits",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AncientParchment
            )
            
            Text(
                text = "Begin your magical journey.\nSave tomes to fill your shelves!",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = CandlelightGlow.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            
            // Call to action button
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                EnchantedGreen,
                                EnchantedGreen.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = GildedGold,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 28.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✨",
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Explore Library",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GildedGold
                    )
                }
            }
        }
    }
}
