package tn.esprit.libraryapp.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.models.Review
import tn.esprit.libraryapp.ui.theme.*
import tn.esprit.libraryapp.viewModel.BookViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════════════════════════
// ANCIENT TOME VIEWER - Immersive Book Details Experience
// ═══════════════════════════════════════════════════════════════════════════════

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun BookDetailsScreen(
    navController: NavHostController,
    bookId: Int,
    viewModel: BookViewModel = viewModel(),
) {
    LaunchedEffect(Unit) { viewModel.setInitialBook(bookId) }

    val book by viewModel.bookDetails.collectAsState()
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val systemUiController = rememberSystemUiController()
    var isImageLoaded by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                viewModel.setInitialBook(bookId)
                isRefreshing = false
            }
        },
    )

    LaunchedEffect(Unit) { 
        systemUiController.setStatusBarColor(Color.Transparent, false) 
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepLibraryBrown)
    ) {
        // Mystical background with particles
        MysticalBookBackground()

        // Main content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ══════════════════════════════════════════════════════════════
            // FLOATING BOOK COVER - 3D Hovering Book Effect
            // ══════════════════════════════════════════════════════════════
            item {
                FloatingBookCover(
                    book = book,
                    isBookmarked = isBookmarked,
                    onBookmarkToggle = { book?.let { viewModel.toggleBookmark(it) } },
                    onBackClick = { 
                        coroutineScope.launch {
                            navController.popBackStack()
                        }
                    },
                    onShareClick = {
                        val shareText = """
                            Check out "${book?.title}" by ${book?.author}!
                            Genre: ${book?.genre}
                            Rating: ${book?.averageRating ?: "Not rated"}
                        """.trimIndent()
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Book"))
                    },
                    onImageLoaded = { isImageLoaded = true }
                )
            }

            // ══════════════════════════════════════════════════════════════
            // BOOK ESSENCE - Title & Author with Magical Styling
            // ══════════════════════════════════════════════════════════════
            item {
                BookEssenceSection(book = book)
            }

            // ══════════════════════════════════════════════════════════════
            // MYSTICAL RATING CONSTELLATION
            // ══════════════════════════════════════════════════════════════
            item {
                book?.let { RatingConstellation(book = it) }
            }

            // ══════════════════════════════════════════════════════════════
            // TOME ATTRIBUTES - Book Info as Magical Runes
            // ══════════════════════════════════════════════════════════════
            item {
                book?.let { TomeAttributesSection(book = it) }
            }

            // ══════════════════════════════════════════════════════════════
            // THE PROPHECY - Book Description as Ancient Scroll
            // ══════════════════════════════════════════════════════════════
            item {
                book?.let { ProphecyScrollSection(description = it.description) }
            }

            // ══════════════════════════════════════════════════════════════
            // BEGIN THE QUEST - Action Buttons
            // ══════════════════════════════════════════════════════════════
            item {
                QuestActionsSection(
                    onStartReading = {
                        try {
                            val encodedUrl = Uri.encode(book?.link)
                            navController.navigate("read_book/$encodedUrl")
                        } catch (e: Exception) {
                            Log.e("BookDetailsScreen", "Error navigating: ${e.message}")
                        }
                    }
                )
            }

            // ══════════════════════════════════════════════════════════════
            // READER'S CHRONICLES - Reviews Section
            // ══════════════════════════════════════════════════════════════
            item {
                book?.let {
                    ReaderChroniclesSection(
                        bookId = it.id,
                        viewModel = viewModel
                    )
                }
            }
        }

        // Pull to refresh
        Box(Modifier.pullRefresh(pullRefreshState)) {
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = RichMahogany,
                contentColor = GildedGold
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MYSTICAL BOOK BACKGROUND
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun MysticalBookBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val particles = remember {
        List(25) {
            BookParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 5f + 2f,
                speed = Random.nextFloat() * 0.15f + 0.05f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Gradient background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    DeepLibraryBrown,
                    MysticPurple.copy(alpha = 0.2f),
                    DeepLibraryBrown
                )
            )
        )

        // Floating particles
        particles.forEach { particle ->
            val animatedY = (particle.y + time * particle.speed) % 1f
            val animatedX = particle.x + sin(animatedY * PI * 2).toFloat() * 0.02f
            val alpha = 0.2f + sin(time * PI * 3 + particle.x * 10).toFloat() * 0.15f

            drawCircle(
                color = GildedGold.copy(alpha = alpha),
                radius = particle.size,
                center = Offset(animatedX * size.width, animatedY * size.height)
            )
        }
    }
}

private data class BookParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float
)

// ═══════════════════════════════════════════════════════════════════════════════
// FLOATING BOOK COVER - 3D Hovering Effect
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun FloatingBookCover(
    book: Book?,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onImageLoaded: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cover")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        // Navigation overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MagicalIconButton(
                icon = Icons.Default.ArrowBack,
                onClick = onBackClick
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MagicalIconButton(
                    icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    onClick = onBookmarkToggle,
                    isActive = isBookmarked
                )
                MagicalIconButton(
                    icon = Icons.Default.Share,
                    onClick = onShareClick
                )
            }
        }

        // Centered floating book
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = floatOffset.dp)
        ) {
            // Glow effect behind book
            Canvas(
                modifier = Modifier
                    .size(200.dp, 280.dp)
                    .align(Alignment.Center)
            ) {
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MysticPurple.copy(alpha = glowPulse),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.maxDimension * 0.8f
                    )
                )
            }

            // Book shadow
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(240.dp)
                    .offset(x = 8.dp, y = 12.dp)
                    .blur(12.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .align(Alignment.Center)
            )

            // Main book cover
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(240.dp)
                    .align(Alignment.Center)
                    .graphicsLayer {
                        shadowElevation = 20f
                        rotationY = 8f
                        cameraDistance = 12f * density
                    }
            ) {
                // Book spine effect
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    WarmLeather.copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                if (book != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.coverImage)
                            .crossfade(true)
                            .build(),
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 3.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        GildedGold,
                                        WarmLeather,
                                        GildedGold
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        onSuccess = { onImageLoaded() }
                    )
                } else {
                    // Shimmer placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shimmer()
                            .background(RichMahogany, RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun MagicalIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    isActive: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isActive) GildedGold.copy(alpha = 0.3f)
                else RichMahogany.copy(alpha = 0.7f)
            )
            .border(
                width = 1.dp,
                color = if (isActive) GildedGold else WarmLeather.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) GildedGold else AncientParchment,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// BOOK ESSENCE SECTION - Title & Author
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun BookEssenceSection(book: Book?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (book != null) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = AncientParchment,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "by ${book.author}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontStyle = FontStyle.Italic
                ),
                color = CandlelightGlow.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Genre badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MysticPurple.copy(alpha = 0.3f))
                    .border(
                        width = 1.dp,
                        color = GildedGold.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = book.genre,
                    style = MaterialTheme.typography.labelMedium,
                    color = GildedGold
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// RATING CONSTELLATION - Star Rating Display
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun RatingConstellation(book: Book) {
    val rating = book.averageRating ?: 0f
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated stars
            repeat(5) { index ->
                val starDelay = index * 100
                val starGlow by infiniteTransition.animateFloat(
                    initialValue = if (index < rating.toInt()) 0.7f else 0.2f,
                    targetValue = if (index < rating.toInt()) 1f else 0.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, delayMillis = starDelay),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "star$index"
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GildedGold.copy(alpha = if (index < rating.toInt()) starGlow * 0.4f else 0f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (index < rating.toInt()) Icons.Filled.Star
                        else if (index < rating) Icons.Default.StarHalf
                        else Icons.Outlined.StarOutline,
                        contentDescription = null,
                        tint = if (index < rating) GildedGold else WarmLeather.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = String.format("%.1f", rating),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = GildedGold
                )
                Text(
                    text = "${book.totalReviews ?: 0} reviews",
                    style = MaterialTheme.typography.labelSmall,
                    color = CandlelightGlow.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// TOME ATTRIBUTES - Book Info as Magical Runes
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun TomeAttributesSection(book: Book) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TomeAttribute(
                icon = Icons.Default.MenuBook,
                value = "${book.numOfPages}",
                label = "Pages"
            )
        }
        item {
            TomeAttribute(
                icon = Icons.Default.CalendarToday,
                value = book.publicationYear.toString(),
                label = "Published"
            )
        }
        item {
            TomeAttribute(
                icon = Icons.Default.Language,
                value = "English",
                label = "Language"
            )
        }
        item {
            TomeAttribute(
                icon = Icons.Default.Category,
                value = book.genre,
                label = "Genre"
            )
        }
    }
}

@Composable
private fun TomeAttribute(
    icon: ImageVector,
    value: String,
    label: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "attr")
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border"
    )

    Box(
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MysticPurple.copy(alpha = 0.3f),
                        RichMahogany.copy(alpha = 0.5f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = GildedGold.copy(alpha = borderGlow),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GildedGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AncientParchment,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = CandlelightGlow.copy(alpha = 0.6f)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PROPHECY SCROLL - Book Description
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun ProphecyScrollSection(description: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            RichMahogany.copy(alpha = 0.6f),
                            MysticPurple.copy(alpha = 0.2f),
                            RichMahogany.copy(alpha = 0.6f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GildedGold.copy(alpha = 0.5f),
                            WarmLeather.copy(alpha = 0.3f),
                            GildedGold.copy(alpha = 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = GildedGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "The Prophecy",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = GildedGold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = description ?: "This ancient tome holds secrets yet to be revealed...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 26.sp
                    ),
                    color = AncientParchment.copy(alpha = 0.9f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// QUEST ACTIONS - Start Reading Button
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun QuestActionsSection(onStartReading: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "action")
    val buttonGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            GildedGold,
                            CandlelightGlow,
                            GildedGold
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            AncientParchment.copy(alpha = buttonGlow),
                            GildedGold.copy(alpha = 0.5f),
                            AncientParchment.copy(alpha = buttonGlow)
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .clickable { onStartReading() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = DeepLibraryBrown,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Begin Your Quest",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = DeepLibraryBrown
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// READER'S CHRONICLES - Reviews Section
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun ReaderChroniclesSection(
    bookId: Int,
    viewModel: BookViewModel
) {
    val reviews by viewModel.reviews.collectAsState(initial = emptyList())
    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(bookId) {
        viewModel.fetchReviews(bookId)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RateReview,
                    contentDescription = null,
                    tint = GildedGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reader's Chronicles",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = AncientParchment
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GildedGold.copy(alpha = 0.2f))
                    .border(1.dp, GildedGold.copy(alpha = 0.5f), CircleShape)
                    .clickable { showReviewDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Review",
                    tint = GildedGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (reviews.isEmpty()) {
            Text(
                text = "No chronicles yet. Be the first to share your journey!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic
                ),
                color = CandlelightGlow.copy(alpha = 0.6f)
            )
        } else {
            reviews.forEach { review ->
                ChronicleCard(review = review, bookId = bookId, viewModel = viewModel)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Review Dialog
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            containerColor = RichMahogany,
            title = {
                Text(
                    "Share Your Chronicle",
                    color = GildedGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(5) { index ->
                            IconButton(onClick = { rating = index + 1 }) {
                                Icon(
                                    imageVector = if (index < rating) Icons.Filled.Star
                                    else Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    tint = if (index < rating) GildedGold
                                    else WarmLeather.copy(alpha = 0.5f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Your thoughts...", color = CandlelightGlow.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AncientParchment,
                            unfocusedTextColor = AncientParchment,
                            focusedBorderColor = GildedGold,
                            unfocusedBorderColor = WarmLeather.copy(alpha = 0.5f),
                            cursorColor = GildedGold
                        ),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rating > 0) {
                            coroutineScope.launch {
                                viewModel.addReview(bookId, rating, reviewText.ifEmpty { null })
                                showReviewDialog = false
                                reviewText = ""
                                rating = 0
                            }
                        }
                    },
                    enabled = rating > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GildedGold,
                        contentColor = DeepLibraryBrown
                    )
                ) {
                    Text("Submit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Cancel", color = CandlelightGlow)
                }
            }
        )
    }
}

@Composable
private fun ChronicleCard(
    review: Review,
    bookId: Int,
    viewModel: BookViewModel
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        RichMahogany.copy(alpha = 0.5f),
                        MysticPurple.copy(alpha = 0.2f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = WarmLeather.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < review.rating) Icons.Filled.Star
                            else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (index < review.rating) GildedGold
                            else WarmLeather.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = review.dateAdded.substring(0, 10),
                        style = MaterialTheme.typography.labelSmall,
                        color = CandlelightGlow.copy(alpha = 0.5f)
                    )
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF6B35).copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (review.comment.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AncientParchment.copy(alpha = 0.9f)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = RichMahogany,
            title = { Text("Delete Chronicle", color = GildedGold) },
            text = { Text("Remove this chronicle from the archives?", color = AncientParchment) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReview(bookId.toString(), review.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B35)
                    )
                ) {
                    Text("Delete", color = AncientParchment)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = CandlelightGlow)
                }
            }
        )
    }
}

@Composable
private fun BookDetailsContent(
    book: Book?,
    contentAlpha: Float,
    onStartReading: () -> Unit,
    viewModel: BookViewModel,
) {
    if (book == null) {
        ShimmerLoadingEffect()
        return
    }

    Log.d("tag", book.toString())
    val reviews = viewModel.reviews.collectAsState(initial = emptyList()).value
    val coroutineScope = rememberCoroutineScope()
    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }

    val slideIn = remember { Animatable(initialValue = 100f) }
    val fadeIn = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(Unit) {
        slideIn.animateTo(targetValue = 0f, animationSpec = tween(500, easing = EaseOutExpo))
        fadeIn.animateTo(targetValue = 1f, animationSpec = tween(700))
    }

    LaunchedEffect(book.id) {
        Log.d("BookDetailsContent", "Fetching reviews for book: ${book.id}")
        viewModel.fetchReviews(book.id)
    }

    LaunchedEffect(reviews) {
        Log.d("BookDetailsContent", "Reviews updated. Count: ${reviews.size}")
        reviews.forEach { review ->
            Log.d(
                "BookDetailsContent",
                "Review: id=${review.id}, rating=${review.rating}, comment=${review.comment}",
            )
        }
    }

    LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp),
            ) {
                // Blurred background
                AsyncImage(
                    model = book.coverImage,
                    contentDescription = null,
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .blur(radius = 20.dp)
                        .graphicsLayer(alpha = 0.3f),
                    contentScale = ContentScale.FillBounds,
                )

                Box(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                listOf(
                                    MaterialTheme.colorScheme
                                        .surface.copy(
                                            alpha = 0.7f,
                                        ),
                                    MaterialTheme.colorScheme
                                        .surface,
                                ),
                            ),
                        ),
                )

                // Book cover and details
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Book cover with animation
                    Surface(
                        modifier =
                        Modifier
                            .width(220.dp)
                            .height(320.dp)
                            .graphicsLayer(alpha = fadeIn.value)
                            .offset(y = (-slideIn.value).dp),
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                    ) {
                        AsyncImage(
                            model = book.coverImage,
                            contentDescription = "Book cover",
                            contentScale = ContentScale.Crop,
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Book title and author with animation
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer(alpha = fadeIn.value),
                    ) {
                        Text(
                            text = book.title,
                            style =
                            MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "by ${book.author}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }

        // Add average rating display if available
        item {
            if (book.averageRating != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Average Rating: ${String.format("%.1f", book.averageRating)} ",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "(${book.totalReviews ?: 0} reviews)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            }
        }

        // Quick Info Cards
        item {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                QuickInfoCard(icon = Icons.Filled.Category, label = "Genre", value = book.genre)
                QuickInfoCard(
                    icon = Icons.Filled.AccessTime,
                    label = "Pages",
                    value = "${book.numOfPages} pages",
                )
                QuickInfoCard(
                    icon = Icons.Filled.Language,
                    label = "Language",
                    value = "English", // Add language to your Book model
                )
            }
        }

        // Book Description
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About this book",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = book.description ?: "No description available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 24.sp,
                    )
                }
            }
        }

        // Action Buttons
        item {
            Spacer(modifier = Modifier.height(24.dp))
            ActionButtons(onStartReading)
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Reviews Section
        item {
            ReviewsSection(
                reviews = reviews,
                onAddReview = { showReviewDialog = true },
                isLoading = false, // Remove uiState dependency
                error = null, // Remove uiState dependency
            )
        }

        // Review Items
        items(reviews) { review -> ReviewItem(review = review, book = book) }
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Add Review") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        repeat(5) { index ->
                            IconButton(onClick = { rating = index + 1 }) {
                                Icon(
                                    imageVector =
                                    if (index < rating) {
                                        Icons.Filled.Star
                                    } else {
                                        Icons.Outlined.StarOutline
                                    },
                                    contentDescription = "Star ${index + 1}",
                                    tint =
                                    if (index < rating) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Your Review (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Write your review here...") },
                        singleLine = false,
                        minLines = 3,
                        maxLines = 5,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (rating > 0) {
                                // Pass the review text even if empty
                                viewModel.addReview(
                                    book.id,
                                    rating,
                                    reviewText.ifEmpty { null },
                                )
                                showReviewDialog = false
                                reviewText = ""
                                rating = 0
                            }
                        }
                    },
                    enabled = rating > 0, // Only rating is required
                ) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun QuickInfoCard(icon: ImageVector, label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.padding(4.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ReviewsSection(
    reviews: List<Review>,
    onAddReview: () -> Unit,
    isLoading: Boolean,
    error: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Reviews (${reviews.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            FilledTonalIconButton(onClick = onAddReview, shape = CircleShape) {
                Icon(Icons.Default.Star, "Add Review")
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
        } else if (error != null) {
            Text(
                text = "Error loading reviews: $error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp),
            )
        } else if (reviews.isEmpty()) {
            Text(
                text = "No reviews yet. Be the first to review!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ActionButtons(onStartReading: () -> Unit) {
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val animatedScale by
        animateFloatAsState(
            targetValue = if (animatedProgress == 1f) 1f else 0.8f,
            animationSpec = spring(dampingRatio = 0.7f),
        )

    LaunchedEffect(Unit) {
        delay(300)
        animatedProgress = 1f
    }

    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onStartReading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
            elevation =
            ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp,
            ),
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Start Reading",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun ReviewItem(review: Review, book: Book, modifier: Modifier = Modifier) {
    val viewModel: BookViewModel = viewModel()
    Log.d("ReviewItem", "Rendering review: id=${review.id}, rating=${review.rating}")
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var animatedProgress by remember { mutableFloatStateOf(0f) }
    val animatedAlpha by
        animateFloatAsState(targetValue = animatedProgress, animationSpec = tween(500))

    LaunchedEffect(Unit) { animatedProgress = 1f }

    Surface(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer(
                alpha = animatedAlpha,
                translationX = (1f - animatedAlpha) * 100f,
            ),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector =
                            if (index < review.rating) {
                                Icons.Filled.Star
                            } else {
                                Icons.Outlined.StarOutline
                            },
                            contentDescription = null,
                            tint =
                            if (index < review.rating) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            },
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = review.dateAdded.substring(0, 10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )

                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete review",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            if (review.comment.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = review.comment,
                    style =
                    MaterialTheme.typography.bodyLarge.copy(
                        lineHeight =
                        MaterialTheme.typography.bodyLarge.lineHeight * 1.3,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Review") },
            text = { Text("Are you sure you want to delete this review?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReview(book.id.toString(), review.id)
                        showDeleteConfirmation = false
                    },
                    colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ShimmerLoadingEffect() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .shimmer(),
    ) {
        // Cover image placeholder
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(Color.LightGray.copy(alpha = 0.5f)),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title placeholder
        Box(
            modifier =
            Modifier
                .fillMaxWidth(0.7f)
                .height(24.dp)
                .background(Color.LightGray.copy(alpha = 0.5f)),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Author placeholder
        Box(
            modifier =
            Modifier
                .fillMaxWidth(0.4f)
                .height(16.dp)
                .background(Color.LightGray.copy(alpha = 0.5f)),
        )

        // Add more shimmer placeholders for other content
    }
}

// Add error handling component
@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) { Text("Retry") }
    }
}
