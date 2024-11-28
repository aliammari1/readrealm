package tn.esprit.libraryapp.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.viewModel.BookViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: BookViewModel = BookViewModel()
) {
    val books by viewModel.books.observeAsState(emptyList())
    var selectedGenre by remember { mutableStateOf(Genre.ALL) }
    val genres = listOf(Genre.ALL, Genre.ACTION, Genre.Adventure, Genre.FANTASY)

    LaunchedEffect(Unit) {
        viewModel.fetchBooks(Genre.ALL)
        Log.d("booksLaunchedEffect", books.toString())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Book Carousels",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        item {
            GenreChipGroup(
                genres = genres,
                selectedGenre = selectedGenre,
                onGenreSelected = { selectedGenre = it }
            )
        }
        if (selectedGenre == Genre.ALL) {
            genres.filter { it != Genre.ALL }.forEach { genre ->
                item {
                    GenreBookCarousel(
                        genre = genre,
                        books = books.filter { it.genre == genre.value }
                    )
                }
            }
        } else {
            item {
                GenreBookCarousel(
                    genre = selectedGenre,
                    books = books.filter { it.genre == selectedGenre.value }
                )
            }
        }
    }
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
            .padding(horizontal = 4.dp)
            .clickable { onGenreSelected(genre) },
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = genre.value,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun GenreBookCarousel(genre: Genre, books: List<Book>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = genre.value,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(modifier = modifier) {
            items(books) { book ->
                BookCard(book = book)
            }
        }
    }
}

@Composable
fun BookCard(book: Book, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .width(200.dp)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(book.coverImage)
                        .build(),
                    contentDescription = "Book cover for ${book.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onLoading = { _ ->
                        Log.d("BookCard", "Loading image for ${book.title}")
                    },
                    onSuccess = { _ ->
                        Log.d("BookCard", "Image loaded for ${book.title}")
                    },
                    onError = { error ->
                        Log.d("BookCard", "Error loading image for ${book.title}: ${error.result.throwable}")
                    }
                    )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Published: ${book.publicationDate}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pages: ${book.numOfPages}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Genre: ${book.genre}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}