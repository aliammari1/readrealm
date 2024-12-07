package tn.esprit.libraryapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import tn.esprit.libraryapp.viewModel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookScreen(
    navController: NavHostController,
    bookId: Int,
    viewModel: BookViewModel = viewModel()
) {
    val book by viewModel.bookDetails.observeAsState()
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    val ebookUiState by viewModel.ebookUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeAudio(context)
        viewModel.fetchBookDetails(bookId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAudio()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Book cover
            Card(
                modifier = Modifier
                    .size(300.dp)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                if (book != null) {
                    AsyncImage(
                        model = book?.coverImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback controls for streaming by title
            ElevatedButton(
                onClick = {
                    book?.title?.let { title ->
                        if (ebookUiState.isPlaying) {
                            viewModel.stopAudio()
                        } else {
                            viewModel.playBookAudio(title)
                        }
                    }
                },
                enabled = !ebookUiState.isLoading && book?.title != null
            ) {
                Icon(
                    imageVector = if (ebookUiState.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (ebookUiState.isPlaying) "Stop" else "Play Book"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (ebookUiState.isPlaying) "Stop" else "Play Book")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // Custom text input section
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter custom text to read") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp),
                maxLines = 10
            )

            // Custom text playback controls
            ElevatedButton(
                onClick = {
                    if (ebookUiState.isPlaying) {
                    } else if (text.isNotEmpty()) {
                    }
                },
                enabled = !ebookUiState.isLoading && (text.isNotEmpty() || ebookUiState.isPlaying)
            ) {
                Icon(
                    imageVector = if (ebookUiState.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (ebookUiState.isPlaying) "Stop" else "Play Custom Text"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (ebookUiState.isPlaying) "Stop" else "Play Custom Text")
            }

            if (ebookUiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            if (ebookUiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = ebookUiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}