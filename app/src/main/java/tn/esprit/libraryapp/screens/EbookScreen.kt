package tn.esprit.libraryapp.screens

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import tn.esprit.libraryapp.viewModel.BookViewModel
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookScreen(
    navController: NavHostController,
    bookId: Int,
    viewModel: BookViewModel = BookViewModel()
) {
    val book by viewModel.bookDetails.observeAsState()
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    val mediaPlayer = remember { MediaPlayer() }

    LaunchedEffect(Unit) {
        viewModel.fetchBookDetails(bookId)
        val audioFile = File(context.filesDir, "audio.mp3")
        if (audioFile.exists()) {
            mediaPlayer.setDataSource(audioFile.path)
            mediaPlayer.prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book?.title ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = {
                        mediaPlayer.stop()
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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

            Spacer(modifier = Modifier.height(32.dp))

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    mediaPlayer.seekTo(mediaPlayer.currentPosition - 10000)
                }) {
                    Icon(Icons.Default.PlayArrow, "Rewind")
                }

                FloatingActionButton(
                    onClick = {
                        if (isPlaying) {
                            mediaPlayer.pause()
                        } else {
                            mediaPlayer.start()
                        }
                        isPlaying = !isPlaying
                    }
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }

                IconButton(onClick = {
                    mediaPlayer.seekTo(mediaPlayer.currentPosition + 10000)
                }) {
                    Icon(Icons.Default.ArrowForward, "Forward")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Show current time / duration
            Text(
                text = "Playing Chapter 1",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}