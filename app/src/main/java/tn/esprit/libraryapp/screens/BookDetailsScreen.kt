package tn.esprit.libraryapp.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson
import kotlinx.coroutines.launch
import tn.esprit.libraryapp.models.Book

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    navController: NavHostController,
    bookId: Int,
    bookJson: String
) {
    val book = remember {
        // Decode the URI component and parse the JSON
        val decodedJson = Uri.decode(bookJson)
        Gson().fromJson(decodedJson, Book::class.java)
    }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val systemUiController = rememberSystemUiController()
    var isImageLoaded by remember { mutableStateOf(false) }

    // Animations
    val imageScale by animateFloatAsState(
        targetValue = if (isImageLoaded) 1f else 0.8f,
        animationSpec = tween(500)
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isImageLoaded) 1f else 0f,
        animationSpec = tween(500)
    )

    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(Color.Transparent, true)
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = isImageLoaded,
                enter = fadeIn() + slideInVertically()
            ) {
                TopAppBar(
                    title = { },
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
                                    .scale(1.2f)
                                    .padding(4.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        }
    ) {  _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Book Cover Image with Parallax Effect
            val imageOffset = (scrollState.value * 0.5f).toFloat()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .graphicsLayer {
                        translationY = imageOffset
                    }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(book.coverImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(imageScale),
                    onSuccess = { isImageLoaded = true }
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 300f
                            )
                        )
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(400.dp))

                AnimatedVisibility(
                    visible = isImageLoaded,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically()
                ) {
                    BookDetailsContent(
                        book = book,
                        contentAlpha = contentAlpha,
                        onStartReading = { /* TODO */ },
                        onStartListening = { navController.navigate("ebook/${bookId}") }
                    )
                }
            }
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
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
    ) {
        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.graphicsLayer { alpha = contentAlpha }
        )

        AnimatedVisibility(
            visible = contentAlpha > 0f,
            enter = fadeIn() + expandVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "by ${book.author}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                BookInfoGrid(book)

                Spacer(modifier = Modifier.height(24.dp))

                ActionButtons(
                    onStartReading = onStartReading,
                    onStartListening = onStartListening
                )
            }
        }
    }
}

@Composable
private fun BookInfoGrid(book: Book) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
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
            icon = Icons.Default.Check,
            label = "Genre",
            value = book.genre,
            delay = 300
        )
    }
}

@Composable
private fun AnimatedBookInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    delay: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically()
    ) {
        BookInfoItem(icon = icon, label = label, value = value)
    }
}

@Composable
private fun BookInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
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
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActionButtons(
    onStartReading: () -> Unit,
    onStartListening: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ElevatedButton(
            onClick = onStartReading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Reading")
        }

        OutlinedButton(
            onClick = onStartListening,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Call, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Listen to Audiobook")
        }
    }
}

@Composable
private fun LoadingAnimation() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.scale(1.2f),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
