package tn.esprit.libraryapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import tn.esprit.libraryapp.viewModel.AuthViewModel



@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: AuthViewModel = AuthViewModel()
) {
    val books = listOf(
        Book(
            "Book Title 1", 
            "2021-01-01", 
            300, 
            "https://diybookcovers.com/wp-content/uploads/2023/07/scifi4thumb.jpg"
        ),
        Book(
            "Book Title 2", 
            "2020-05-15", 
            250, 
            "https://diybookcovers.com/wp-content/uploads/2023/07/scifi4thumb.jpg"
        ),
        Book(
            "Book Title 3", 
            "2019-08-20", 
            400, 
            "https://diybookcovers.com/wp-content/uploads/2023/07/scifi4thumb.jpg"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Book Carousel",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        BookCarousel(books = books)
    }
}


@Composable
fun BookCarousel(books: List<Book>, modifier: Modifier = Modifier) {
    LazyRow(modifier = modifier) {
        items(books) { book ->
            BookCard(book = book)
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
                    model = book.coverImage,
                    contentDescription = "Book cover for ${book.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
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
        }
    }
}