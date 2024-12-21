package tn.esprit.libraryapp.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.models.Review
import tn.esprit.libraryapp.viewModel.BookViewModel
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    navController: NavHostController,
    bookId: Int,
    bookJson: String,
    initialIsBookmarked: Boolean = false,
) {
    val viewModel: BookViewModel = viewModel()
    val initialBook = remember {
        val decodedJson = Uri.decode(bookJson)
        Gson().fromJson(decodedJson, Book::class.java)
    }

    val book by viewModel.bookDetails.observeAsState(initialBook)
    val isBookmarked by viewModel.isBookmarked.observeAsState(initial = initialIsBookmarked)
    val coroutineScope = rememberCoroutineScope()
    val systemUiController = rememberSystemUiController()
    var isImageLoaded by remember { mutableStateOf(false) }

    // Initialize bookmark state when screen loads
    LaunchedEffect(Unit) {
        Log.d("BookDetailsScreen", "Initial isBookmarked state: $initialIsBookmarked $isBookmarked")
        //viewModel.fetchBookDetails(bookId)
    }

    // Update bookmark state when book details change
    LaunchedEffect(book) {
        Log.d("BookDetailsScreen", "Book details updated: ${book.id}")
        if (book != initialBook) {
            viewModel.initializeBookmarkState(book.bookmarks)
        }
    }

    val imageScale by animateFloatAsState(
        targetValue = if (isImageLoaded) 1f else 0.8f, animationSpec = tween(500)
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isImageLoaded) 1f else 0f, animationSpec = tween(500)
    )

    LaunchedEffect(Unit) { systemUiController.setStatusBarColor(Color.Transparent, true) }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = isImageLoaded,
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically()
            ) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    systemUiController.setStatusBarColor(Color.Red)
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                "Back",
                                modifier = Modifier
                                    .scale(1.3f)
                                    .padding(4.dp),
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleBookmark(book) }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Toggle Bookmark",
                                tint = Color.White,
                                modifier = Modifier.scale(1.2f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent.copy(alpha = 0.2f),
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    modifier = Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent),
                            startY = 0f,
                            endY = 100f
                        )
                    )
                )
            }
        }
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Book Cover Image with Parallax Effect
            val imageOffset = (rememberScrollState().value * 0.5f).toFloat()
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .graphicsLayer {
                    translationY = imageOffset
                }) {
                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(book.coverImage)
                    .crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(imageScale),
                    onSuccess = { isImageLoaded = true })

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent, Color.Black.copy(
                                        alpha = 0.7f
                                    )
                                ), startY = 300f
                            )
                        )
                )
            }

            // Content
            BookDetailsContent(
                book = book,
                contentAlpha = contentAlpha,
                onStartReading = { /* TODO */ },
                onStartListening = { navController.navigate("ebook/${bookId}") }
            )
        }
    }
}

@Composable
private fun BookDetailsContent(
    book: Book,
    contentAlpha: Float,
    onStartReading: () -> Unit,
    onStartListening: () -> Unit
) {
    val viewModel: BookViewModel = viewModel()
    val reviews = viewModel.reviews.collectAsState(initial = emptyList()).value
    val uiState by viewModel.ebookUiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }

    // Add animation states
    val slideIn = remember { Animatable(initialValue = 100f) }
    val fadeIn = remember { Animatable(initialValue = 0f) }
    
    LaunchedEffect(Unit) {
        slideIn.animateTo(
            targetValue = 0f,
            animationSpec = tween(500, easing = EaseOutExpo)
        )
        fadeIn.animateTo(
            targetValue = 1f,
            animationSpec = tween(700)
        )
    }

    LaunchedEffect(book.id) {
        Log.d("BookDetailsContent", "Fetching reviews for book: ${book.id}")
        viewModel.fetchReviews(book.id)
    }

    // Debug logging
    LaunchedEffect(reviews) {
        Log.d("BookDetailsContent", "Reviews updated. Count: ${reviews.size}")
        reviews.forEach { review ->
            Log.d(
                "BookDetailsContent",
                "Review: id=${review.id}, rating=${review.rating}, comment=${review.comment}"
            )
        }
    }

    LaunchedEffect(uiState) {
        Log.d(
            "BookDetailsContent",
            "UI State: isLoading=${uiState.isLoading}, error=${uiState.error}"
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
            ) {
                // Blurred background
                AsyncImage(
                    model = book.coverImage,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(radius = 20.dp)
                        .graphicsLayer(alpha = 0.3f),
                    contentScale = ContentScale.FillBounds
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                )

                // Book cover and details
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Book cover with animation
                    Surface(
                        modifier = Modifier
                            .width(220.dp)
                            .height(320.dp)
                            .graphicsLayer(alpha = fadeIn.value)
                            .offset(y = (-slideIn.value).dp),
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp
                    ) {
                        AsyncImage(
                            model = book.coverImage,
                            contentDescription = "Book cover",
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Book title and author with animation
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer(alpha = fadeIn.value)
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "by ${book.author}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Quick Info Cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickInfoCard(
                    icon = Icons.Filled.Category,
                    label = "Genre",
                    value = book.genre
                )
                QuickInfoCard(
                    icon = Icons.Filled.AccessTime,
                    label = "Pages",
                    value = "${book.numOfPages} pages"
                )
                QuickInfoCard(
                    icon = Icons.Filled.Language,
                    label = "Language",
                    value = "English"  // Add language to your Book model
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
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "About this book",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = book.title ?: "No description available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // Action Buttons
        item {
            Spacer(modifier = Modifier.height(24.dp))
            ActionButtons(onStartReading, onStartListening)
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Reviews Section
        item {
            ReviewsSection(
                reviews = reviews,
                onAddReview = { showReviewDialog = true },
                isLoading = uiState.isLoading,
                error = uiState.error
            )
        }

        // Review Items
        items(reviews) { review ->
            ReviewItem(review = review, book = book)
        }
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Add Review") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(5) { index ->
                            IconButton(onClick = { rating = index + 1 }) {
                                Icon(
                                    imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Star ${index + 1}",
                                    tint = if (index < rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Your Review") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (rating > 0) {
                                viewModel.addReview(book.id, rating, reviewText)
                                showReviewDialog = false
                                reviewText = ""
                                rating = 0
                            }
                        }
                    },
                    enabled = rating > 0
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QuickInfoCard(
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReviewsSection(
    reviews: List<Review>,
    onAddReview: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reviews (${reviews.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            FilledTonalIconButton(
                onClick = onAddReview,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Star, "Add Review")
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Text(
                text = "Error loading reviews: $error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        } else if (reviews.isEmpty()) {
            Text(
                text = "No reviews yet. Be the first to review!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BookInfoGrid(book: Book) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        AnimatedBookInfoItem(
            icon = Icons.Default.DateRange,
            label = "Published",
            value = book.publicationDate.toString(),
            delay = 100
        )
        AnimatedBookInfoItem(
            icon = Icons.Outlined.Check,
            label = "Pages",
            value = book.numOfPages.toString(),
            delay = 200
        )
        AnimatedBookInfoItem(
            icon = Icons.Default.Check, label = "Genre", value = book.genre, delay = 300
        )
    }
}

@Composable
private fun AnimatedBookInfoItem(icon: ImageVector, label: String, value: String, delay: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }

    AnimatedVisibility(visible = visible, enter = fadeIn() + expandVertically()) {
        BookInfoItem(icon = icon, label = label, value = value)
    }
}

@Composable
private fun BookInfoItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActionButtons(onStartReading: () -> Unit, onStartListening: () -> Unit) {
    var animatedProgress by remember { mutableStateOf(0f) }
    val animatedScale by animateFloatAsState(
        targetValue = if (animatedProgress == 1f) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.7f)
    )

    LaunchedEffect(Unit) {
        delay(300)
        animatedProgress = 1f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer(
                scaleX = animatedScale,
                scaleY = animatedScale
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onStartReading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Start Reading",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        OutlinedButton(
            onClick = onStartListening,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                Icons.Default.Call,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Listen to Audiobook",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun LoadingAnimation() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.scale(1.2f), color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ReviewItem(review: Review, book: Book, modifier: Modifier = Modifier) {
    val viewModel: BookViewModel = viewModel()
    Log.d("ReviewItem", "Rendering review: id=${review.id}, rating=${review.rating}")
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var animatedProgress by remember { mutableStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(500)
    )

    LaunchedEffect(Unit) {
        animatedProgress = 1f
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer(
                alpha = animatedAlpha,
                translationX = (1f - animatedAlpha) * 100f
            ),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < review.rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (index < review.rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = review.dateAdded.substring(0, 10),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete review",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (review.comment.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3
                    ),
                    color = MaterialTheme.colorScheme.onSurface
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
