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
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
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
import kotlinx.coroutines.delay
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
import tn.esprit.libraryapp.ui.theme.*
import tn.esprit.libraryapp.viewModel.BookViewModel
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════════════════════════
// READREALM ENCHANTED LIBRARY THEME
// ═══════════════════════════════════════════════════════════════════════════════

private var currentJob: Job? = null

// Magical floating particles for ambient effect
@Composable
private fun EnchantedParticles(
    modifier: Modifier = Modifier,
    particleCount: Int = 20,
    particleColor: Color = GildedGold.copy(alpha = 0.4f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val particles = remember {
        List(particleCount) {
            ParticleState(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.3f + 0.1f,
                alpha = Random.nextFloat() * 0.5f + 0.2f
            )
        }
    }

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val animatedY = (particle.y + time * particle.speed) % 1f
            val animatedX = particle.x + sin(animatedY * PI * 2).toFloat() * 0.02f
            val flickerAlpha = particle.alpha * (0.7f + sin(time * PI * 4 + particle.x * 10).toFloat() * 0.3f)

            drawCircle(
                color = particleColor.copy(alpha = flickerAlpha),
                radius = particle.size,
                center = Offset(animatedX * size.width, animatedY * size.height)
            )
        }
    }
}

private data class ParticleState(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)

// Ornate decorative border
@Composable
private fun OrnateFrame(
    modifier: Modifier = Modifier,
    cornerSize: Dp = 20.dp,
    strokeWidth: Dp = 2.dp,
    color: Color = GildedGold
) {
    val infiniteTransition = rememberInfiniteTransition(label = "frame")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Canvas(modifier = modifier) {
        val corner = cornerSize.toPx()
        val stroke = strokeWidth.toPx()

        // Draw ornate corners
        listOf(
            Offset(0f, 0f) to 0f,
            Offset(size.width, 0f) to 90f,
            Offset(size.width, size.height) to 180f,
            Offset(0f, size.height) to 270f
        ).forEach { (offset, rotation) ->
            rotate(rotation, pivot = offset) {
                // Corner flourish
                drawPath(
                    path = Path().apply {
                        moveTo(offset.x, offset.y + corner)
                        lineTo(offset.x, offset.y)
                        lineTo(offset.x + corner, offset.y)
                        // Decorative curve
                        quadraticTo(
                            offset.x + corner * 0.3f,
                            offset.y + corner * 0.3f,
                            offset.x,
                            offset.y + corner
                        )
                    },
                    color = color.copy(alpha = glowAlpha),
                    style = Stroke(width = stroke)
                )
            }
        }

        // Draw connecting lines with dashed effect
        drawRoundRect(
            color = color.copy(alpha = 0.3f),
            cornerRadius = CornerRadius(corner / 2),
            style = Stroke(
                width = stroke / 2,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        )
    }
}

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

    val searchBarOffset by animateDpAsState(
        targetValue = if (hasSuggestions) 40.dp else 80.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "searchBarOffset",
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        ),
        exit = fadeOut() + slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            // Enchanted backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                DeepLibraryBrown.copy(alpha = 0.95f),
                                MysticPurple.copy(alpha = 0.9f),
                                DeepLibraryBrown.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .clickable {
                        focusManager.clearFocus()
                        onDismiss()
                    },
            ) {
                EnchantedParticles(
                    modifier = Modifier.fillMaxSize(),
                    particleCount = 15,
                    particleColor = GildedGold.copy(alpha = 0.2f)
                )
            }

            // Enchanted Search Container
            Box(
                modifier = modifier
                    .padding(top = searchBarOffset)
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                RichMahogany.copy(alpha = 0.9f),
                                DeepLibraryBrown.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GildedGold.copy(alpha = 0.6f),
                                WarmLeather.copy(alpha = 0.3f),
                                GildedGold.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .graphicsLayer {
                        shadowElevation = 16f
                    }
            ) {
                Column {
                    // Search Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Enchanted Search Field
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(DeepLibraryBrown.copy(alpha = 0.6f))
                                .border(
                                    width = 1.dp,
                                    color = GildedGold.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(28.dp)
                                )
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                modifier = Modifier.fillMaxSize(),
                                placeholder = {
                                    Text(
                                        "Search the archives...",
                                        color = CandlelightGlow.copy(alpha = 0.5f)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = GildedGold
                                    )
                                },
                                trailingIcon = {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                showFilters = !showFilters
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.FilterList,
                                                "Search filters",
                                                tint = GildedGold.copy(alpha = 0.7f)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                    putExtra(
                                                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                                    )
                                                }
                                                voiceRecognizer.launch(intent)
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Mic,
                                                "Voice search",
                                                tint = GildedGold.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = AncientParchment,
                                    unfocusedTextColor = AncientParchment,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = GildedGold
                                ),
                                singleLine = true,
                            )
                        }
                    }

                    // Search Filters
                    AnimatedVisibility(visible = showFilters) {
                        EnchantedSearchFilters(
                            currentFilters = searchFilters,
                            onFilterChange = viewModel::updateSearchFilters,
                        )
                    }

                    // Search History
                    if (searchQuery.isEmpty() && searchHistory.isNotEmpty()) {
                        EnchantedSearchHistory(
                            history = searchHistory,
                            onHistoryItemClick = onSearchQueryChange,
                            onClearHistory = viewModel::clearSearchHistory,
                            onRemoveHistoryItem = viewModel::removeFromSearchHistory,
                        )
                    }

                    // Search Results
                    if (hasSuggestions) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            GildedGold.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .padding(vertical = 8.dp),
                        ) {
                            items(suggestions) { book ->
                                BookSearchResult(
                                    book = book,
                                    onBookClick = {
                                        onSuggestionSelected(book)
                                        onDismiss()
                                    },
                                )
                            }
                        }
                    } else if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = CandlelightGlow.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No tomes found in the archives...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontStyle = FontStyle.Italic
                                    ),
                                    color = CandlelightGlow.copy(alpha = 0.6f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnchantedSearchFilters(currentFilters: SearchFilter, onFilterChange: (SearchFilter) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Filter by Realm",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = GildedGold,
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
                                genre = if (currentFilters.genre == genre) null else genre
                            )
                        )
                    },
                    label = { 
                        Text(
                            genre.value,
                            color = if (currentFilters.genre == genre) DeepLibraryBrown else AncientParchment
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GildedGold,
                        containerColor = MysticPurple.copy(alpha = 0.3f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = WarmLeather.copy(alpha = 0.3f),
                        selectedBorderColor = GildedGold,
                        enabled = true,
                        selected = currentFilters.genre == genre
                    )
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
                    onClick = { onFilterChange(currentFilters.copy(sortBy = sortOption)) },
                    label = { 
                        Text(
                            sortOption.name.replace("_", " "),
                            color = if (currentFilters.sortBy == sortOption) DeepLibraryBrown else AncientParchment
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CandlelightGlow,
                        containerColor = MysticPurple.copy(alpha = 0.3f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = WarmLeather.copy(alpha = 0.3f),
                        selectedBorderColor = CandlelightGlow,
                        enabled = true,
                        selected = currentFilters.sortBy == sortOption
                    )
                )
            }
        }
    }
}

@Composable
private fun EnchantedSearchHistory(
    history: List<SearchHistory>,
    onHistoryItemClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    onRemoveHistoryItem: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Recent Searches",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = GildedGold
            )
            TextButton(onClick = onClearHistory) {
                Text(
                    "Clear All",
                    color = CandlelightGlow.copy(alpha = 0.7f)
                )
            }
        }

        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
            items(history) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHistoryItemClick(item.query) }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(18.dp),
                            tint = WarmLeather.copy(alpha = 0.6f)
                        )
                        Text(
                            item.query,
                            color = AncientParchment.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(
                        onClick = { onRemoveHistoryItem(item.query) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            "Remove from history",
                            tint = CandlelightGlow.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepLibraryBrown,
                        RichMahogany.copy(alpha = 0.95f),
                        DeepLibraryBrown
                    )
                )
            )
    ) {
        // Ambient magical particles
        EnchantedParticles(
            modifier = Modifier.fillMaxSize(),
            particleCount = 25,
            particleColor = GildedGold.copy(alpha = 0.3f)
        )

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .semantics { contentDescription = "ReadRealm Library" },
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            // ══════════════════════════════════════════════════════════════
            // ENCHANTED HEADER - The Grand Library Entrance
            // ══════════════════════════════════════════════════════════════
            item {
                EnchantedLibraryHeader(
                    onSearchClick = { isSearchVisible = true },
                    onCameraClick = { isCameraVisible = true }
                )
            }

            // ══════════════════════════════════════════════════════════════
            // MYSTICAL READING STATS - Achievement Scrolls
            // ══════════════════════════════════════════════════════════════
            item {
                MysticalReadingStats(
                    booksRead = 12,
                    readingTime = "32h",
                    currentStreak = 5
                )
            }

            // ══════════════════════════════════════════════════════════════
            // FEATURED TOME - The Legendary Book Display
            // ══════════════════════════════════════════════════════════════
            item {
                if (books.isNotEmpty()) {
                    LegendaryTomeCard(
                        book = books.random(),
                        navController = navController
                    )
                }
            }

            // ══════════════════════════════════════════════════════════════
            // REALM SELECTOR - Magical Genre Portals
            // ══════════════════════════════════════════════════════════════
            item {
                RealmPortalSelector(
                    genres = Genre.entries,
                    selectedGenre = selectedGenre,
                    onGenreSelected = { selectedGenre = it }
                )
            }

            // ══════════════════════════════════════════════════════════════
            // CONTINUE YOUR QUEST - Reading Progress Scrolls
            // ══════════════════════════════════════════════════════════════
            item {
                QuestProgressSection(
                    books = books.take(3),
                    navController = navController
                )
            }

            // ══════════════════════════════════════════════════════════════
            // BOOK COLLECTIONS - The Infinite Shelves
            // ══════════════════════════════════════════════════════════════
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
                                EnchantedBookshelf(
                                    genre = genre,
                                    books = books.filter {
                                        it.genre.equals(genre.value, ignoreCase = true)
                                    },
                                    navController = navController,
                                )
                            } else {
                                ShimmerBookshelf(genre = genre)
                            }
                        }
                    }
                } else {
                    item {
                        if (loadedGenres.contains(selectedGenre)) {
                            EnchantedBookshelf(
                                genre = selectedGenre,
                                books = books.filter {
                                    it.genre.equals(selectedGenre.value, ignoreCase = true)
                                },
                                navController = navController,
                            )
                        } else {
                            ShimmerBookshelf(genre = selectedGenre)
                        }
                    }
                }
            }
        }

        // Camera Modal
        if (isCameraVisible) {
            CameraModal(
                isVisible = isCameraVisible,
                onDismiss = { isCameraVisible = false },
                onTextDetected = { detectedText ->
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

// ═══════════════════════════════════════════════════════════════════════════════
// ENCHANTED LIBRARY HEADER
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun EnchantedLibraryHeader(
    onSearchClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // App Title with magical styling
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // Animated book icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GildedGold.copy(alpha = glowPulse * 0.5f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = GildedGold,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "ReadRealm",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            letterSpacing = 2.sp
                        ),
                        color = AncientParchment
                    )
                    Text(
                        text = "Enter the realm of infinite stories",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 1.sp
                        ),
                        color = CandlelightGlow.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search & Scan Actions - Magical Orb Style
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Orb
                MagicalActionOrb(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Search,
                    label = "Search the Archives",
                    onClick = onSearchClick
                )

                // Camera Scan Orb
                MagicalActionOrb(
                    modifier = Modifier.weight(0.4f),
                    icon = Icons.Default.CameraAlt,
                    label = "Scan",
                    onClick = onCameraClick,
                    isCompact = true
                )
            }
        }
    }
}

@Composable
private fun MagicalActionOrb(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isCompact: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border"
    )

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MysticPurple.copy(alpha = 0.4f),
                        InkBlue.copy(alpha = 0.3f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        GildedGold.copy(alpha = borderGlow),
                        WarmLeather.copy(alpha = borderGlow * 0.5f),
                        GildedGold.copy(alpha = borderGlow)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GildedGold,
                modifier = Modifier.size(22.dp)
            )
            if (!isCompact) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AncientParchment.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MYSTICAL READING STATISTICS
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun MysticalReadingStats(
    booksRead: Int,
    readingTime: String,
    currentStreak: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stats")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Decorative frame
        OrnateFrame(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            color = GildedGold.copy(alpha = 0.5f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AchievementOrb(
                icon = Icons.Default.MenuBook,
                value = booksRead.toString(),
                label = "Tomes Read",
                accentColor = GildedGold
            )

            // Decorative divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(80.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GildedGold.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            AchievementOrb(
                icon = Icons.Default.Timer,
                value = readingTime,
                label = "Hours Spent",
                accentColor = CandlelightGlow
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(80.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GildedGold.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            AchievementOrb(
                icon = Icons.Default.Whatshot,
                value = "$currentStreak",
                label = "Day Streak",
                accentColor = Color(0xFFFF6B35)
            )
        }
    }
}

@Composable
private fun AchievementOrb(
    icon: ImageVector,
    value: String,
    label: String,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "achievement")
    val iconGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = iconGlow * 0.3f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = AncientParchment
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CandlelightGlow.copy(alpha = 0.6f)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// LEGENDARY TOME CARD - Featured Book
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun LegendaryTomeCard(
    book: Book,
    navController: NavHostController
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "tome")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                navController.navigate("book_details/${book.id}")
            }
    ) {
        // Background Image with overlay
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(book.coverImage)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(8.dp)
        )

        // Gradient overlays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepLibraryBrown.copy(alpha = 0.7f),
                            MysticPurple.copy(alpha = 0.8f),
                            DeepLibraryBrown.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Shimmer effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.3f }
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            GildedGold.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        start = Offset(shimmer * 500, 0f),
                        end = Offset((shimmer + 0.5f) * 500, 500f)
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book Cover with 3D effect
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(180.dp)
                    .graphicsLayer {
                        shadowElevation = 24f
                        rotationY = 5f
                        cameraDistance = 12f * density
                    }
            ) {
                // Book spine shadow
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                AsyncImage(
                    model = book.coverImage,
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    GildedGold.copy(alpha = 0.8f),
                                    WarmLeather.copy(alpha = 0.5f)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Book Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Featured badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            GildedGold.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GildedGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LEGENDARY TOME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GildedGold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    ),
                    color = AncientParchment,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "by ${book.author}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = CandlelightGlow.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Read Now Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(GildedGold, WarmLeather)
                            )
                        )
                        .clickable {
                            navController.navigate("book_details/${book.id}")
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Begin Quest",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = DeepLibraryBrown
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = DeepLibraryBrown,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// REALM PORTAL SELECTOR - Genre Selection
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun RealmPortalSelector(
    genres: List<Genre>,
    selectedGenre: Genre,
    onGenreSelected: (Genre) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = null,
                tint = GildedGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Explore Realms",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = AncientParchment
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(genres) { genre ->
                RealmPortal(
                    genre = genre,
                    isSelected = genre == selectedGenre,
                    onClick = { onGenreSelected(genre) }
                )
            }
        }
    }
}

@Composable
private fun RealmPortal(
    genre: Genre,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "portal")
    val portalGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val backgroundColor = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                MysticPurple.copy(alpha = 0.8f),
                InkBlue.copy(alpha = 0.6f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                RichMahogany.copy(alpha = 0.4f),
                DeepLibraryBrown.copy(alpha = 0.6f)
            )
        )
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = if (isSelected) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            GildedGold.copy(alpha = portalGlow),
                            CandlelightGlow.copy(alpha = portalGlow)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            WarmLeather.copy(alpha = 0.3f),
                            WarmLeather.copy(alpha = 0.2f)
                        )
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(
            text = genre.value,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) GildedGold else AncientParchment.copy(alpha = 0.8f)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// QUEST PROGRESS SECTION - Continue Reading
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun QuestProgressSection(
    books: List<Book>,
    navController: NavHostController
) {
    if (books.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null,
                tint = GildedGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Continue Your Quest",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = AncientParchment
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        books.forEach { book ->
            QuestScrollCard(
                book = book,
                progress = Random.nextFloat() * 0.6f + 0.1f,
                navController = navController
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuestScrollCard(
    book: Book,
    progress: Float,
    navController: NavHostController
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        RichMahogany.copy(alpha = 0.6f),
                        MysticPurple.copy(alpha = 0.3f),
                        RichMahogany.copy(alpha = 0.5f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = WarmLeather.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                navController.navigate("book_details/${book.id}")
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Book cover
            AsyncImage(
                model = book.coverImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(55.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = GildedGold.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AncientParchment,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = CandlelightGlow.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar styled as magical energy
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(DeepLibraryBrown.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(GildedGold, CandlelightGlow)
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Progress percentage in magical orb
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MysticPurple.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = GildedGold
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ENCHANTED BOOKSHELF - Book Grid Display
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun EnchantedBookshelf(
    genre: Genre,
    books: List<Book>,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Section Header with decorative elements
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decorative line
            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, GildedGold.copy(alpha = 0.5f))
                        )
                    )
            )

            Text(
                text = "  ${genre.value}  ",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = GildedGold
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(GildedGold.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Books in horizontal scroll - styled as books on a shelf
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(books) { book ->
                EnchantedTomeCard(
                    book = book,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun ReadingStatisticsCard(booksRead: Int, readingTime: String, currentStreak: Int) {
    // Legacy component - kept for compatibility but using new theme
    MysticalReadingStats(booksRead, readingTime, currentStreak)
}

@Composable
fun StatisticItem(value: String, label: String, icon: ImageVector) {
    AchievementOrb(
        icon = icon,
        value = value,
        label = label,
        accentColor = GildedGold
    )
}

@Composable
fun ContinueReadingItem(book: Book, navController: NavHostController) {
    val haptic = LocalHapticFeedback.current
    QuestScrollCard(
        book = book,
        progress = 0.3f,
        navController = navController
    )
}

@Composable
fun FeaturedBookCard(book: Book, navController: NavHostController) {
    LegendaryTomeCard(book = book, navController = navController)
}

@Composable
fun ContinueReadingSection(books: List<Book>, navController: NavHostController) {
    QuestProgressSection(books = books, navController = navController)
}

@Composable
fun BookGridSection(genre: Genre, books: List<Book>, navController: NavHostController) {
    EnchantedBookshelf(genre = genre, books = books, navController = navController)
}

@Composable
fun GenreChipGroup(genres: List<Genre>, selectedGenre: Genre, onGenreSelected: (Genre) -> Unit) {
    RealmPortalSelector(genres = genres, selectedGenre = selectedGenre, onGenreSelected = onGenreSelected)
}

@Composable
fun GenreChip(genre: Genre, isSelected: Boolean, onGenreSelected: (Genre) -> Unit) {
    RealmPortal(genre = genre, isSelected = isSelected, onClick = { onGenreSelected(genre) })
}

// ═══════════════════════════════════════════════════════════════════════════════
// ENCHANTED TOME CARD - Individual Book Display
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalCoilApi::class)
@Composable
private fun EnchantedTomeCard(
    book: Book,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "tome")
    val bookGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .width(140.dp)
            .height(220.dp)
            .scale(scale)
            .graphicsLayer {
                shadowElevation = 16f
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                navController.navigate("book_details/${book.id}")
            }
    ) {
        // Book spine shadow
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .offset(x = (-3).dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column {
            // Book Cover
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(book.coverImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Magical overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MysticPurple.copy(alpha = 0.3f)
                                ),
                                startY = 100f
                            )
                        )
                )

                // Glowing border
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    GildedGold.copy(alpha = bookGlow),
                                    WarmLeather.copy(alpha = bookGlow * 0.5f),
                                    GildedGold.copy(alpha = bookGlow)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                )

                // Rating badge
                book.averageRating?.let { rating ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(
                                DeepLibraryBrown.copy(alpha = 0.85f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GildedGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("%.1f", rating),
                                style = MaterialTheme.typography.labelSmall,
                                color = AncientParchment,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Book Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AncientParchment,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = book.author,
                    style = MaterialTheme.typography.labelSmall,
                    color = CandlelightGlow.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// SHIMMER LOADING EFFECTS - Mystical Style
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun ShimmerBookCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "translate"
    )

    val shimmerColors = listOf(
        RichMahogany.copy(alpha = 0.3f),
        GildedGold.copy(alpha = 0.15f),
        RichMahogany.copy(alpha = 0.3f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 200f, translateAnim + 200f)
    )

    Box(
        modifier = modifier
            .width(140.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DeepLibraryBrown.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = WarmLeather.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Shimmer for book cover
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Shimmer for title
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Shimmer for author
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
private fun ShimmerBookshelf(genre: Genre) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, GildedGold.copy(alpha = 0.3f))
                        )
                    )
            )

            Text(
                text = "  ${genre.value}  ",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = GildedGold.copy(alpha = 0.6f)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(GildedGold.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(4) { ShimmerBookCard() }
        }
    }
}

@Composable
fun ShimmerBookGrid(genre: Genre) {
    ShimmerBookshelf(genre = genre)
}

// ═══════════════════════════════════════════════════════════════════════════════
// BOOK CARD - Enchanted Style  
// ═══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalCoilApi::class)
@Composable
fun BookCard(book: Book, navController: NavHostController, modifier: Modifier = Modifier) {
    EnchantedTomeCard(book = book, navController = navController, modifier = modifier)
}

// ═══════════════════════════════════════════════════════════════════════════════
// BOOK ROW - Enchanted Style
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
fun BookRow(genre: Genre, books: List<Book>, navController: NavHostController) {
    if (books.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = genre.value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = GildedGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No ancient tomes discovered in this realm...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic
                ),
                color = CandlelightGlow.copy(alpha = 0.6f)
            )
        }
        return
    }

    EnchantedBookshelf(genre = genre, books = books, navController = navController)
}

// ═══════════════════════════════════════════════════════════════════════════════
// ENCHANTED SEARCH RESULT
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun BookSearchResult(book: Book, onBookClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onBookClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = RichMahogany.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Book cover with enchanted border
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(70.dp)
            ) {
                AsyncImage(
                    model = book.coverImage,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                        .border(
                            width = 1.dp,
                            color = GildedGold.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentScale = ContentScale.Crop,
                )
            }

            // Book details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AncientParchment,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CandlelightGlow.copy(alpha = 0.7f),
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
                        tint = GildedGold.copy(alpha = 0.6f),
                    )
                    Text(
                        text = book.publicationYear.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = CandlelightGlow.copy(alpha = 0.5f),
                    )
                }
            }

            // Arrow icon
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "View book details",
                tint = GildedGold,
            )
        }
    }
    
    // Decorative divider
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        WarmLeather.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
    )
}
