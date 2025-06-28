package tn.esprit.libraryapp.screens

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.palette.graphics.Palette
import coil3.BitmapImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.models.SearchFilter
import tn.esprit.libraryapp.models.SearchHistory
import tn.esprit.libraryapp.models.SortOption
import tn.esprit.libraryapp.viewModel.BookViewModel
import java.util.concurrent.TimeUnit

private var currentJob: Job? = null

private suspend fun connectToBookEventStream(genre: Genre, viewModel: BookViewModel) {
    try {
        // Cancel any existing job before starting a new one
        currentJob?.cancel()
        currentJob = currentCoroutineContext()[Job]

        viewModel.setLoading(true)
        viewModel.initializeBooks()

        if (genre == Genre.ALL) {
            supervisorScope {
                val genreJobs =
                    Genre.entries.filter { it != Genre.ALL }.map { specificGenre,
                        ->
                        async {
                            connectToSpecificGenreStream(
                                specificGenre,
                                viewModel,
                            )
                        }
                    }
                genreJobs.awaitAll()
            }
        } else {
            connectToSpecificGenreStream(genre, viewModel)
        }
    } catch (e: Exception) {
        Log.e("HomeScreen", "Error in SSE connections: ${e.message}")
    } finally {
        viewModel.setLoading(false)
        viewModel.setInitialLoadDone()
    }
}

private suspend fun connectToSpecificGenreStream(genre: Genre, viewModel: BookViewModel) {
    withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder().readTimeout(0, TimeUnit.SECONDS).build()

        // Create a mutable variable to hold the response
        var response: Response? = null

        try {
            response =
                client.newCall(
                    Request.Builder()
                        .url(
                            "https://libraryapp-nest-back.vercel.app/book/genre/${genre.value.lowercase()}",
                        )
                        .addHeader("Accept", "text/event-stream")
                        .build(),
                )
                    .execute()

            if (!response.isSuccessful) {
                Log.e(
                    "HomeScreen",
                    "Failed to connect to SSE for genre ${genre.value}: ${response.code}",
                )
                return@withContext
            }

            val source = response.body?.source() ?: return@withContext

            while (!source.exhausted() && currentCoroutineContext().isActive) {
                val line = source.readUtf8Line() ?: continue

                if (line.startsWith("data:")) {
                    val bookJson = line.removePrefix("data:").trim()
                    try {
                        val book =
                            Gson().fromJson(bookJson, Book::class.java)
                        withContext(Dispatchers.Main) {
                            viewModel.addBookToGenre(genre, book)
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "HomeScreen",
                            "Error parsing book JSON for genre ${genre.value}: ${e.message}",
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(
                "HomeScreen",
                "Error in SSE connection for genre ${genre.value}: ${e.message}",
            )
        } finally {
            // Clean up resources
            response?.close()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    suggestions: List<Book>,
    onSuggestionSelected: (Book) -> Unit,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LocalContext.current
    val haptic = LocalHapticFeedback.current
    val viewModel: BookViewModel = viewModel()

    val searchHistory by viewModel.searchHistory.collectAsState()
    val searchFilters by viewModel.searchFilters.collectAsState()
    var showFilters by remember { mutableStateOf(false) }

    val voiceRecognizer =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.let { onSearchQueryChange(it) }
        }

    val focusManager = LocalFocusManager.current
    val hasSuggestions by remember(suggestions) { derivedStateOf { suggestions.isNotEmpty() } }

    // Animate the vertical position of the search bar
    val searchBarOffset by
        animateDpAsState(
            targetValue = if (hasSuggestions) 40.dp else 80.dp,
            animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            label = "searchBarOffset",
        )

    AnimatedVisibility(
        visible = isVisible,
        enter =
        fadeIn() +
            slideInVertically(
                initialOffsetY = { -it },
                animationSpec =
                spring(
                    dampingRatio =
                    Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            ),
        exit =
        fadeOut() +
            slideOutVertically(
                targetOffsetY = { -it },
                animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            ),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            // Scrim / background overlay with fade animation
            Box(
                modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable {
                        focusManager.clearFocus()
                        onDismiss()
                    },
            )

            // Search bar and suggestions
            Surface(
                modifier =
                modifier.padding(top = searchBarOffset)
                    .fillMaxWidth(0.9f)
                    .graphicsLayer {
                        clip = true
                        shape = RoundedCornerShape(16.dp)
                        shadowElevation = 8f
                    },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Search books...") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                )
                            },
                            trailingIcon = {
                                Row {
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(
                                                HapticFeedbackType
                                                    .TextHandleMove,
                                            )
                                            showFilters =
                                                !showFilters
                                        },
                                    ) {
                                        Icon(
                                            Icons.Default
                                                .FilterList,
                                            "Search filters",
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(
                                                HapticFeedbackType
                                                    .TextHandleMove,
                                            )
                                            val intent =
                                                Intent(
                                                    RecognizerIntent
                                                        .ACTION_RECOGNIZE_SPEECH,
                                                )
                                                    .apply {
                                                        putExtra(
                                                            RecognizerIntent
                                                                .EXTRA_LANGUAGE_MODEL,
                                                            RecognizerIntent
                                                                .LANGUAGE_MODEL_FREE_FORM,
                                                        )
                                                    }
                                            voiceRecognizer
                                                .launch(
                                                    intent,
                                                )
                                        },
                                    ) {
                                        Icon(
                                            Icons.Default
                                                .Mic,
                                            "Voice search",
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors =
                            TextFieldDefaults.colors(
                                unfocusedTextColor =
                                MaterialTheme
                                    .colorScheme
                                    .outline
                                    .copy(
                                        alpha =
                                        0.3f,
                                    ),
                            ),
                            singleLine = true,
                        )
                    }

                    // Search Filters
                    AnimatedVisibility(visible = showFilters) {
                        SearchFilters(
                            currentFilters = searchFilters,
                            onFilterChange =
                            viewModel::updateSearchFilters,
                        )
                    }

                    // Search History
                    if (searchQuery.isEmpty() && searchHistory.isNotEmpty()) {
                        SearchHistorySection(
                            history = searchHistory,
                            onHistoryItemClick = onSearchQueryChange,
                            onClearHistory =
                            viewModel::clearSearchHistory,
                            onRemoveHistoryItem =
                            viewModel::removeFromSearchHistory,
                        )
                    }

                    // Search Results
                    if (hasSuggestions) {
                        Divider()
                        LazyColumn(
                            modifier =
                            Modifier.fillMaxWidth()
                                .heightIn(max = 350.dp),
                        ) {
                            items(suggestions) { book ->
                                BookSearchResult(
                                    book = book,
                                    onBookClick = {
                                        onSuggestionSelected(
                                            book,
                                        )
                                        onDismiss()
                                    },
                                )
                            }
                        }
                    } else if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier =
                            Modifier.fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No results found",
                                style =
                                MaterialTheme.typography
                                    .bodyMedium,
                                color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchFilters(currentFilters: SearchFilter, onFilterChange: (SearchFilter) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            text = "Filters",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        // Genre Filter
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(Genre.entries) { genre ->
                FilterChip(
                    selected = currentFilters.genre == genre,
                    onClick = {
                        onFilterChange(
                            currentFilters.copy(
                                genre =
                                if (currentFilters.genre ==
                                    genre
                                ) {
                                    null
                                } else {
                                    genre
                                },
                            ),
                        )
                    },
                    label = { Text(genre.value) },
                )
            }
        }

        // Sort Options
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(SortOption.entries) { sortOption ->
                FilterChip(
                    selected = currentFilters.sortBy == sortOption,
                    onClick = {
                        onFilterChange(
                            currentFilters.copy(sortBy = sortOption),
                        )
                    },
                    label = { Text(sortOption.name.replace("_", " ")) },
                )
            }
        }
    }
}

@Composable
private fun SearchHistorySection(
    history: List<SearchHistory>,
    onHistoryItemClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    onRemoveHistoryItem: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Recent Searches", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onClearHistory) { Text("Clear All") }
        }

        LazyColumn {
            items(history) { item ->
                Row(
                    modifier =
                    Modifier.fillMaxWidth()
                        .clickable {
                            onHistoryItemClick(item.query)
                        }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text(item.query)
                    }
                    IconButton(onClick = { onRemoveHistoryItem(item.query) }) {
                        Icon(Icons.Default.Close, "Remove from history")
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedDetectedText(text: String, isBook: Boolean) {
    var animationTriggered by remember { mutableStateOf(false) }

    val rotation by
        animateFloatAsState(
            targetValue = if (animationTriggered) 360f else 0f,
            animationSpec = tween(1000),
            label = "",
        )

    val scale by
        animateFloatAsState(
            targetValue = if (animationTriggered) 1.2f else 1f,
            animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
            label = "",
        )

    LaunchedEffect(text) { animationTriggered = true }

    Text(
        text = text,
        style =
        TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textDecoration =
            if (isBook) {
                TextDecoration.Underline
            } else {
                TextDecoration.None
            },
            color = if (isBook) MaterialTheme.colorScheme.primary else Color.Red,
        ),
        modifier =
        Modifier.graphicsLayer {
            transformOrigin = TransformOrigin.Center
            rotationX = rotation
            scaleX = scale
            scaleY = scale
        }
            .padding(16.dp),
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CameraModal(isVisible: Boolean, onDismiss: () -> Unit, onTextDetected: (String) -> Unit) {
    if (!isVisible) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    val textRecognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    // Initialize object detector
    val options = remember {
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .build()
    }
    val objectDetector = remember { ObjectDetection.getClient(options) }

    var detectedText by remember { mutableStateOf<String?>(null) }
    var isBook by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted ->
            if (isGranted) {
                // Camera permission granted
            }
        }

    LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.CAMERA) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize()) { preview,
            ->
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                    preview.surfaceProvider = previewView.surfaceProvider

                    val imageAnalyzer =
                        ImageAnalysis.Builder()
                            .setBackpressureStrategy(
                                ImageAnalysis
                                    .STRATEGY_KEEP_ONLY_LATEST,
                            )
                            .build()

                    imageAnalyzer.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                    ) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image =
                                InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy
                                        .imageInfo
                                        .rotationDegrees,
                                )

                            // First detect if there's a book in the
                            // image
                            objectDetector
                                .process(image)
                                .addOnSuccessListener {
                                        detectedObjects ->
                                    isBook =
                                        detectedObjects
                                            .any { obj,
                                                ->
                                                // Check if the object is book-shaped
                                                // (rectangular with typical book
                                                // dimensions)
                                                val boundingBox =
                                                    obj.boundingBox
                                                val ratio =
                                                    boundingBox
                                                        .height()
                                                        .toFloat() /
                                                        boundingBox
                                                            .width()
                                                ratio in
                                                    1.2f..2.0f // Typical book aspect
                                                // ratios
                                            }

                                    // If a book is detected,
                                    // proceed with text
                                    // recognition
                                    if (isBook) {
                                        textRecognizer
                                            .process(
                                                image,
                                            )
                                            .addOnSuccessListener {
                                                    visionText,
                                                ->
                                                if (visionText
                                                        .text
                                                        .isNotEmpty()
                                                ) {
                                                    detectedText =
                                                        visionText
                                                            .text
                                                    onTextDetected(
                                                        visionText
                                                            .text,
                                                    )
                                                }
                                            }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer,
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                ContextCompat.getMainExecutor(context),
            )
        }

        // Display detected text with 3D animation
        detectedText?.let { text ->
            Box(
                modifier =
                Modifier.fillMaxWidth()
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.7f)),
            ) { AnimatedDetectedText(text = text, isBook = isBook) }
        }

        // Status message
        Text(
            text = if (isBook) "Book detected!" else "Point camera at a book",
            color = if (isBook) Color.Green else Color.White,
            modifier =
            Modifier.align(Alignment.TopCenter)
                .padding(top = 32.dp)
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(8.dp),
        )

        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close camera",
                tint = Color.White,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val viewModel: BookViewModel = viewModel()
    val books by viewModel.books.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState(emptyList())
    val loadedGenres by viewModel.loadedGenres.collectAsState()
    val initialLoadDone by viewModel.initialLoadDone.collectAsState()
    var selectedGenre by remember { mutableStateOf(Genre.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var isCameraVisible by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    DisposableEffect(Unit) {
        onDispose {
            currentJob?.cancel()
            currentJob = null
        }
    }

    LaunchedEffect(selectedGenre) {
        if (!initialLoadDone) {
            connectToBookEventStream(selectedGenre, viewModel)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier =
            modifier.fillMaxSize().semantics {
                contentDescription = "Home Screen Book List"
            },
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                Surface(
                    modifier =
                    Modifier.fillMaxWidth().shadow(8.dp).semantics {
                        heading()
                    },
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                ) {
                    Column(
                        modifier =
                        Modifier.padding(
                            horizontal = 24.dp,
                            vertical = 16.dp,
                        )
                            .padding(top = 8.dp),
                    ) {
                        Text(
                            text = "Discover",
                            style =
                            MaterialTheme.typography
                                .headlineMedium.copy(
                                    fontWeight =
                                    FontWeight.Bold,
                                    fontSize = 32.sp,
                                ),
                        )
                        Text(
                            text = "Find your next book to read",
                            style =
                            MaterialTheme.typography.bodyLarge
                                .copy(
                                    color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurface
                                        .copy(
                                            alpha =
                                            0.7f,
                                        ),
                                ),
                            modifier = Modifier.padding(top = 4.dp),
                        )

                        Row(
                            modifier =
                            Modifier.fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement =
                            Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(
                                        HapticFeedbackType
                                            .TextHandleMove,
                                    )
                                    isSearchVisible = true
                                },
                                modifier =
                                Modifier.weight(1f)
                                    .semantics {
                                        contentDescription =
                                            "Search Books Button"
                                    },
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                )
                                Spacer(
                                    modifier =
                                    Modifier.width(8.dp),
                                )
                                Text("Search books...")
                            }

                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(
                                        HapticFeedbackType
                                            .TextHandleMove,
                                    )
                                    isCameraVisible = true
                                },
                                modifier =
                                Modifier.semantics {
                                    contentDescription =
                                        "Scan Book with Camera"
                                },
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(
                                    Icons.Default.Camera,
                                    contentDescription =
                                    "Scan book",
                                )
                            }
                        }
                    }
                }
            }

            item {
                ReadingStatisticsCard(
                    booksRead = 12,
                    readingTime = "32h",
                    currentStreak = 5,
                )
            }

            item {
                if (books.isNotEmpty()) {
                    FeaturedBookCard(
                        book = books.random(),
                        navController = navController,
                    )
                }
            }

            item {
                GenreChipGroup(
                    genres = Genre.entries,
                    selectedGenre = selectedGenre,
                    onGenreSelected = { selectedGenre = it },
                )
            }

            item {
                ContinueReadingSection(
                    books = books.take(3),
                    navController = navController,
                )
            }

            if (searchQuery.isNotEmpty()) {
                item {
                    BookRow(
                        genre = Genre.ALL,
                        books = searchResults,
                        navController = navController,
                    )
                }
            } else {
                if (selectedGenre == Genre.ALL) {
                    Genre.entries.filter { it != Genre.ALL }.forEach { genre ->
                        item {
                            if (loadedGenres.contains(genre)) {
                                BookGridSection(
                                    genre = genre,
                                    books =
                                    books.filter {
                                        it.genre
                                            .equals(
                                                genre.value,
                                                ignoreCase =
                                                true,
                                            )
                                    },
                                    navController =
                                    navController,
                                )
                            } else {
                                ShimmerBookGrid(genre = genre)
                            }
                        }
                    }
                } else {
                    item {
                        if (loadedGenres.contains(selectedGenre)) {
                            BookGridSection(
                                genre = selectedGenre,
                                books =
                                books.filter {
                                    it.genre.equals(
                                        selectedGenre
                                            .value,
                                        ignoreCase =
                                        true,
                                    )
                                },
                                navController = navController,
                            )
                        } else {
                            ShimmerBookGrid(genre = selectedGenre)
                        }
                    }
                }
            }
        }

        // Add Camera Modal
        if (isCameraVisible) {
            CameraModal(
                isVisible = isCameraVisible,
                onDismiss = { isCameraVisible = false },
                onTextDetected = { detectedText ->
                    // Update search query with detected text
                    searchQuery = detectedText
                    viewModel.onSearchQueryChange(detectedText)
                    isSearchVisible = true
                },
            )
        }

        // Modal Search Bar
        ModalSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { query ->
                searchQuery = query
                viewModel.onSearchQueryChange(query)
            },
            suggestions = searchResults,
            onSuggestionSelected = { book ->
                navController.navigate("book_details/${book.id}")
            },
            isVisible = isSearchVisible,
            onDismiss = { isSearchVisible = false },
        )
    }
}

@Composable
fun ReadingStatisticsCard(booksRead: Int, readingTime: String, currentStreak: Int) {
    Card(
        modifier =
        Modifier.fillMaxWidth().padding(16.dp).semantics {
            contentDescription =
                "Reading Statistics: $booksRead books read, $readingTime reading time, $currentStreak day streak"
        },
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatisticItem(
                value = booksRead.toString(),
                label = "Books Read",
                icon = Icons.Default.Book,
            )
            StatisticItem(
                value = readingTime,
                label = "Reading Time",
                icon = Icons.Default.Timer,
            )
            StatisticItem(
                value = "$currentStreak days",
                label = "Current Streak",
                icon = Icons.Default.Star, // Changed from LocalFire to Star
            )
        }
    }
}

@Composable
fun StatisticItem(value: String, label: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun ContinueReadingItem(book: Book, navController: NavHostController) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier =
        Modifier.fillMaxWidth()
            .height(80.dp)
            .clickable {
                haptic.performHapticFeedback(
                    HapticFeedbackType.TextHandleMove,
                )
                navController.navigate("book_details/${book.id}")
            }
            .semantics {
                contentDescription =
                    "Continue reading ${book.title} by ${book.author}, Progress: 30%"
            },
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Book cover
            AsyncImage(
                model = book.coverImage,
                contentDescription = null,
                modifier = Modifier.width(60.dp).fillMaxHeight(),
                contentScale = ContentScale.Crop,
            )

            // Book info
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                LinearProgressIndicator(
                    progress = 0.3f, // Replace with actual reading progress
                    modifier =
                    Modifier.padding(top = 4.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                )
            }

            // Progress percentage
            Text(
                text = "30%", // Replace with actual percentage
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun FeaturedBookCard(book: Book, navController: NavHostController) {
    Card(
        modifier =
        Modifier.fillMaxWidth()
            .padding(16.dp)
            .height(200.dp)
            .clickable { /* Navigate to book details */ },
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = book.coverImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier =
                Modifier.fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(
                                    alpha = 0.7f,
                                ),
                            ),
                        ),
                    ),
            )

            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(
                    text = "Featured Book of the Day",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
fun ContinueReadingSection(books: List<Book>, navController: NavHostController) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Continue Reading",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        books.forEach { book ->
            ContinueReadingItem(book = book, navController = navController)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun BookGridSection(genre: Genre, books: List<Book>, navController: NavHostController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = genre.value, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        // Wrap grid in a fixed height container
        Box(
            modifier =
            Modifier.height(400.dp), // Fixed height to prevent infinite height
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxHeight(),
            ) {
                items(books.size) { index ->
                    BookCard(book = books[index], navController = navController)
                }
            }
        }
    }
}

@Composable
fun BookRow(genre: Genre, books: List<Book>, navController: NavHostController) {
    if (books.isEmpty()) {
        // Show a message when no books are available
        Column {
            Text(
                text = genre.value,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            Text(
                text = "No books available for this genre",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
        return
    }

    Column {
        Text(
            text = genre.value,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        LazyRow {
            items(books) { book ->
                BookCard(book = book, navController = navController)
            }
        }
    }
}

@Composable
fun GenreChipGroup(genres: List<Genre>, selectedGenre: Genre, onGenreSelected: (Genre) -> Unit) {
    LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        items(genres) { genre ->
            GenreChip(
                genre = genre,
                isSelected = genre == selectedGenre,
                onGenreSelected = onGenreSelected,
            )
        }
    }
}

@Composable
fun GenreChip(genre: Genre, isSelected: Boolean, onGenreSelected: (Genre) -> Unit) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier =
        Modifier.padding(end = 8.dp)
            .clickable {
                haptic.performHapticFeedback(
                    HapticFeedbackType.TextHandleMove,
                )
                onGenreSelected(genre)
            }
            .semantics {
                contentDescription =
                    "Genre: ${genre.value}, ${if (isSelected) "Selected" else "Not Selected"}"
            },
        shape = RoundedCornerShape(24.dp),
        color =
        if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isSelected) 0.dp else 4.dp,
        border =
        BorderStroke(
            1.dp,
            if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            },
        ),
    ) {
        Text(
            text = genre.value,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style =
            MaterialTheme.typography.labelLarge.copy(
                color =
                if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            ),
        )
    }
}

@Composable
fun ShimmerBookCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "")
    val translateAnim by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec =
            infiniteRepeatable(
                animation =
                tween(
                    durationMillis = 1200,
                    easing = FastOutSlowInEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "",
        )

    val shimmerColors =
        listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f),
        )

    val brush =
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnim, translateAnim),
            end = Offset(translateAnim + 100f, translateAnim + 100f),
        )

    Card(
        modifier = modifier.width(160.dp).padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Shimmer for book cover
            Spacer(
                modifier =
                Modifier.fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Shimmer for title
            Spacer(
                modifier =
                Modifier.fillMaxWidth(0.8f).height(20.dp).background(brush),
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Shimmer for author
            Spacer(
                modifier =
                Modifier.fillMaxWidth(0.6f).height(16.dp).background(brush),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun BookCard(book: Book, navController: NavHostController, modifier: Modifier = Modifier) {
    var dominantColor by remember { mutableStateOf(Color.White) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Card(
        modifier =
        modifier.width(130.dp)
            .height(280.dp)
            .padding(end = 16.dp)
            .clickable {
                haptic.performHapticFeedback(
                    HapticFeedbackType.TextHandleMove,
                )
                try {
                    navController.navigate("book_details/${book.id}")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .semantics {
                contentDescription = "Book: ${book.title} by ${book.author}"
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors =
        CardDefaults.cardColors(
            containerColor =
            dominantColor.copy(
                alpha = 0.5f,
            ), // Increased alpha for more visibility
        ),
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                AsyncImage(
                    model =
                    ImageRequest.Builder(context)
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
                                    .clearFilters() // Remove
                                    // any
                                    // filters
                                    // to get
                                    // more
                                    // colors
                                    .generate { palette ->
                                        palette?.let {
                                            dominantColor =
                                                when {
                                                    it.vibrantSwatch !=
                                                        null ->
                                                        Color(
                                                            it.vibrantSwatch!!
                                                                .rgb,
                                                        )
                                                    it.dominantSwatch !=
                                                        null ->
                                                        Color(
                                                            it.dominantSwatch!!
                                                                .rgb,
                                                        )
                                                    it.mutedSwatch !=
                                                        null ->
                                                        Color(
                                                            it.mutedSwatch!!
                                                                .rgb,
                                                        )
                                                    it.darkVibrantSwatch !=
                                                        null ->
                                                        Color(
                                                            it.darkVibrantSwatch!!
                                                                .rgb,
                                                        )
                                                    it.darkMutedSwatch !=
                                                        null ->
                                                        Color(
                                                            it.darkMutedSwatch!!
                                                                .rgb,
                                                        )
                                                    it.lightVibrantSwatch !=
                                                        null ->
                                                        Color(
                                                            it.lightVibrantSwatch!!
                                                                .rgb,
                                                        )
                                                    it.lightMutedSwatch !=
                                                        null ->
                                                        Color(
                                                            it.lightMutedSwatch!!
                                                                .rgb,
                                                        )
                                                    else ->
                                                        Color.White
                                                }
                                        }
                                    }
                            }
                            else -> {
                                dominantColor = Color.White
                            }
                        }
                    },
                )
            }

            Column(
                modifier =
                Modifier.fillMaxWidth()
                    .padding(12.dp)
                    .background(
                        dominantColor.copy(alpha = 0.2f),
                    ) // Add background to text container
                    .padding(8.dp), // Add padding inside the background
            ) {
                Text(
                    text = book.title,
                    style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun ShimmerBookGrid(genre: Genre) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = genre.value, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.height(400.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxHeight(),
            ) { items(6) { ShimmerBookCard() } }
        }
    }
}

@Composable
private fun BookSearchResult(book: Book, onBookClick: () -> Unit) {
    Surface(
        modifier =
        Modifier.fillMaxWidth()
            .clickable(onClick = onBookClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(72.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Book cover
            AsyncImage(
                model = book.coverImage,
                contentDescription = null,
                modifier =
                Modifier.width(48.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
            )

            // Book details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = book.publicationYear.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Arrow icon
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "View book details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Divider(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
    )
}
