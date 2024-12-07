package tn.esprit.libraryapp.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.palette.graphics.Palette
import coil3.BitmapImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.viewModel.BookViewModel
import android.net.Uri
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: BookViewModel = BookViewModel()
) {
    val books by viewModel.books.observeAsState(emptyList())
    val searchResults by viewModel.searchResults.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(true)
    var selectedGenre by remember { mutableStateOf(Genre.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(selectedGenre) {
        viewModel.fetchBooks(selectedGenre)
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "Discover",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                )
                Text(
                    text = "Find your next book to read",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        viewModel.onSearchQueryChange(query)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    placeholder = { Text("Search books...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                GenreChipGroup(
                    genres = Genre.values().toList(),
                    selectedGenre = selectedGenre,
                    onGenreSelected = {
                        selectedGenre = it
                    }
                )
            }

            if (isLoading) {
                items(3) {
                    ShimmerBookRow()
                }
            } else if (searchQuery.isNotEmpty()) {
                // Show search results
                item {
                    BookRow(
                        genre = Genre.ALL,
                        books = searchResults,
                        navController = navController
                    )
                }
            } else {
                if (selectedGenre == Genre.ALL) {
                    Genre.values()
                        .filter { it != Genre.ALL }
                        .forEach { genre ->
                            item {
                                BookRow(
                                    genre = genre,
                                    books = books.filter { it.genre.equals(genre.value, ignoreCase = true) },
                                    navController = navController
                                )
                            }
                        }
                } else {
                    item {
                        BookRow(
                            genre = selectedGenre,
                            books = books.filter { it.genre.equals(selectedGenre.value, ignoreCase = true) },
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookRow(
    genre: Genre,
    books: List<Book>,
    navController: NavHostController
) {
    if (books.isEmpty()) {
        return
    }

    Column {
        Text(
            text = genre.value,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyRow {
            items(books) { book ->
                BookCard(book = book, navController = navController)
            }
        }
    }
}

@Composable
fun ShimmerBookRow() {
    Column {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(24.dp)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow {
            items(3) {
                ShimmerBookCard()
            }
        }
    }
}

@Composable
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )
    background(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
    )
}

@Composable
fun GenreChipGroup(
    genres: List<Genre>,
    selectedGenre: Genre,
    onGenreSelected: (Genre) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        items(genres) { genre ->
            GenreChip(
                genre = genre,
                isSelected = genre == selectedGenre,
                onGenreSelected = onGenreSelected
            )
        }
    }
}

@Composable
fun GenreChip(
    genre: Genre,
    isSelected: Boolean,
    onGenreSelected: (Genre) -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable { onGenreSelected(genre) },
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 0.dp else 4.dp,
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = genre.value,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun GenreBookCarousel(
    genre: Genre,
    books: List<Book>,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = genre.value,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(modifier = modifier) {
            items(books) { book ->
                BookCard(book = book, navController = navController)
            }
        }
    }
}

@Composable
fun ShimmerGenreCarousel(genre: Genre, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = genre.value,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(modifier = modifier) {
            items(5) {
                ShimmerBookCard()
            }
        }
    }
}

@Composable
fun ShimmerBookCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 100f, translateAnim + 100f)
    )

    Card(
        modifier = modifier
            .width(160.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Shimmer for book cover
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Shimmer for title
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(20.dp)
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Shimmer for author
            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .background(brush)
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun BookCard(
    book: Book,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var dominantColor by remember { mutableStateOf(Color.White) }
    val context = LocalContext.current

    Card(
        modifier = modifier
            .width(130.dp)
            .height(280.dp)
            .padding(end = 16.dp)
            .clickable {
                try {
                    val bookJson = Uri.encode(Gson().toJson(book))
                    navController.navigate("book_details/${book.id}/$bookJson")
                } catch (e: Exception) {
                    // Handle navigation error
                    e.printStackTrace()
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = dominantColor.copy(alpha = 0.5f) // Increased alpha for more visibility
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(book.coverImage)
                        .crossfade(true)
                        .allowHardware(false)
                        .build(),
                    contentDescription = "Book cover for ${book.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onSuccess = { success ->
                        when (val image = success.result.image) {
                            is BitmapImage -> {
                                Palette.Builder(image.bitmap)
                                    .maximumColorCount(16)
                                    .clearFilters() // Remove any filters to get more colors
                                    .generate { palette ->
                                        palette?.let {
                                            dominantColor = when {
                                                it.vibrantSwatch != null -> Color(it.vibrantSwatch!!.rgb)
                                                it.dominantSwatch != null -> Color(it.dominantSwatch!!.rgb)
                                                it.mutedSwatch != null -> Color(it.mutedSwatch!!.rgb)
                                                it.darkVibrantSwatch != null -> Color(it.darkVibrantSwatch!!.rgb)
                                                it.darkMutedSwatch != null -> Color(it.darkMutedSwatch!!.rgb)
                                                it.lightVibrantSwatch != null -> Color(it.lightVibrantSwatch!!.rgb)
                                                it.lightMutedSwatch != null -> Color(it.lightMutedSwatch!!.rgb)
                                                else -> Color.White
                                            }
                                        }
                                    }
                            }

                            else -> {
                                dominantColor = Color.White
                            }
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(dominantColor.copy(alpha = 0.2f)) // Add background to text container
                    .padding(8.dp) // Add padding inside the background
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
