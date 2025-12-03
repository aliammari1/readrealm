package tn.esprit.libraryapp.screens.experience

import android.net.Uri
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
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.viewModel.BookViewModel
import kotlin.math.cos
import kotlin.math.sin

// Enchanted Library Theme Colors
private val DeepLibraryBrown = Color(0xFF2C1810)
private val GildedGold = Color(0xFFD4AF37)
private val MysticPurple = Color(0xFF4A1942)
private val AncientParchment = Color(0xFFF4E4BC)
private val EnchantedForest = Color(0xFF1A3A2A)
private val MidnightBlue = Color(0xFF0D1B2A)
private val RoseGold = Color(0xFFB76E79)
private val CelestialSilver = Color(0xFFC0C0C0)

/**
 * Explore Hub Screen - The gateway to immersive literary experiences
 * Users can browse their library and select books to transform into AR/VR experiences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreHubScreen(
    navController: NavHostController,
    bookViewModel: BookViewModel = viewModel()
) {
    // Use bookmarks as the user's personal library to explore
    val books by bookViewModel.bookmarks.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Fantasy", "Romance", "Mystery", "Sci-Fi", "Classic")
    
    // Animated background
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val starRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stars"
    )
    
    val portalPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    LaunchedEffect(Unit) {
        bookViewModel.loadBookmarks()
        // Give a short delay for loading, then show content
        delay(1000)
        isLoading = false
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MidnightBlue,
                        DeepLibraryBrown.copy(alpha = 0.9f),
                        MysticPurple.copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        // Animated star field background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 3
            
            // Draw rotating constellation
            for (i in 0..50) {
                val angle = (i * 7.2f + starRotation) * (Math.PI / 180f)
                val radius = 50f + (i * 15f) % 400f
                val x = centerX + (cos(angle) * radius).toFloat()
                val y = centerY + (sin(angle) * radius * 0.5f).toFloat()
                val starSize = 1f + (i % 3) * 1.5f
                val alpha = 0.3f + (i % 5) * 0.1f
                
                drawCircle(
                    color = CelestialSilver.copy(alpha = alpha),
                    radius = starSize,
                    center = Offset(x, y)
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Section
            ExploreHeader(portalPulse)
            
            // Category Filter
            CategoryFilterRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            
            // Content
            if (isLoading) {
                LoadingExploreState()
            } else if (books.isEmpty()) {
                EmptyExploreState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Featured Experience Section
                    item {
                        FeaturedExperienceCard(
                            portalPulse = portalPulse,
                            onExplore = {
                                // Navigate with demo book
                                val encodedTitle = Uri.encode("The Enchanted Library Demo")
                                val encodedContent = Uri.encode("Welcome to the Enchanted Library. This is a demo experience where you can explore the magical world of books. Walk through ancient corridors filled with floating tomes, discover hidden passages, and unlock the secrets of literary realms.")
                                navController.navigate("book_experience/demo/$encodedTitle/$encodedContent")
                            }
                        )
                    }
                    
                    // Section Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoStories,
                                contentDescription = null,
                                tint = GildedGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Your Library",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AncientParchment
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${books.size} books",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CelestialSilver.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    // Book Grid
                    items(books.chunked(2)) { rowBooks ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowBooks.forEach { book ->
                                ExploreBookCard(
                                    book = book,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        val encodedTitle = Uri.encode(book.title ?: "Unknown Book")
                                        val encodedContent = Uri.encode(book.description ?: "Explore this literary world.")
                                        navController.navigate("book_experience/${book.id}/$encodedTitle/$encodedContent")
                                    }
                                )
                            }
                            // Fill empty space if odd number of books
                            if (rowBooks.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    
                    // Bottom spacer for navigation bar
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreHeader(portalPulse: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated portal icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(portalPulse)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GildedGold.copy(alpha = 0.6f),
                                MysticPurple.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Explore,
                    contentDescription = "Explore",
                    tint = GildedGold,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "✦ Literary Realms ✦",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = GildedGold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Step into the worlds within your books",
                style = MaterialTheme.typography.bodyMedium,
                color = AncientParchment.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GildedGold.copy(alpha = 0.3f),
                    selectedLabelColor = GildedGold,
                    containerColor = DeepLibraryBrown.copy(alpha = 0.5f),
                    labelColor = AncientParchment.copy(alpha = 0.7f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isSelected) GildedGold else CelestialSilver.copy(alpha = 0.3f),
                    selectedBorderColor = GildedGold,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

@Composable
private fun FeaturedExperienceCard(
    portalPulse: Float,
    onExplore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onExplore),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MysticPurple,
                            DeepLibraryBrown,
                            EnchantedForest.copy(alpha = 0.8f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Magical particles overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (i in 0..30) {
                    val x = (i * 37f) % size.width
                    val y = (i * 23f + portalPulse * 50) % size.height
                    val alpha = 0.3f + (i % 4) * 0.1f
                    
                    drawCircle(
                        color = GildedGold.copy(alpha = alpha),
                        radius = 2f + (i % 3),
                        center = Offset(x, y)
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stars,
                            contentDescription = null,
                            tint = GildedGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FEATURED EXPERIENCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = GildedGold,
                            letterSpacing = 2.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "The Enchanted Library",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AncientParchment
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Explore our demo realm — ancient halls filled with floating tomes and whispered secrets",
                        style = MaterialTheme.typography.bodySmall,
                        color = AncientParchment.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = onExplore,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GildedGold,
                            contentColor = DeepLibraryBrown
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Enter Realm",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Portal visualization
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(portalPulse)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    GildedGold.copy(alpha = 0.8f),
                                    MysticPurple.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = AncientParchment,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExploreBookCard(
    book: Book,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .height(220.dp)
            .scale(scale)
            .clickable {
                isHovered = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepLibraryBrown.copy(alpha = 0.8f)
        ),
        border = BorderStroke(1.dp, GildedGold.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Book cover background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MysticPurple.copy(alpha = 0.7f),
                                DeepLibraryBrown,
                                EnchantedForest.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                DeepLibraryBrown.copy(alpha = 0.7f),
                                DeepLibraryBrown.copy(alpha = 0.95f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Experience badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MysticPurple.copy(alpha = 0.8f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Explore,
                                contentDescription = null,
                                tint = GildedGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AR/VR",
                                style = MaterialTheme.typography.labelSmall,
                                color = GildedGold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Book info
                Column {
                    Text(
                        text = book.title ?: "Unknown Title",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AncientParchment,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = book.author ?: "Unknown Author",
                        style = MaterialTheme.typography.bodySmall,
                        color = CelestialSilver.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Explore button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = GildedGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = GildedGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Enter World",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = GildedGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingExploreState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = GildedGold,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Opening portals to literary realms...",
                style = MaterialTheme.typography.bodyMedium,
                color = AncientParchment.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun EmptyExploreState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.MenuBook,
                contentDescription = null,
                tint = GildedGold.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Books in Your Library",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AncientParchment,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Add books to your library to transform them into immersive AR/VR experiences",
                style = MaterialTheme.typography.bodyMedium,
                color = CelestialSilver.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { /* Navigate to home or add books */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GildedGold,
                    contentColor = DeepLibraryBrown
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Browse Books",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
