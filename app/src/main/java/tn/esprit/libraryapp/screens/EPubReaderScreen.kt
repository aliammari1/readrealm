package tn.esprit.libraryapp.screens

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.concurrent.formatDuration
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import tn.esprit.libraryapp.models.BookmarkPage
import tn.esprit.libraryapp.models.ReadingStatistics
import tn.esprit.libraryapp.models.ReadingTheme
import tn.esprit.libraryapp.utils.ReaderThemes
import tn.esprit.libraryapp.utils.VoiceCommand
import tn.esprit.libraryapp.utils.VoiceCommandHandler
import tn.esprit.libraryapp.viewModel.EPubReaderViewModel
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStreamReader
import java.util.Locale
import java.util.zip.ZipFile
import kotlin.math.abs
import kotlin.math.roundToInt

private val languageToLocale =
    mapOf(
        "en" to Locale.ENGLISH,
        "fr" to Locale.FRENCH,
        "es" to Locale("es"),
        "de" to Locale.GERMAN,
        "it" to Locale.ITALIAN,
        "ar" to Locale("ar")
    )

data class ReadingPreferences(
    val fontSize: Float = 16f,
    val lineHeight: Float = 1.5f,
    val isDarkMode: Boolean = false,
    val backgroundColor: Color = Color.White,
    val textColor: Color = Color.Black,
    val targetLanguage: String = "en",
    val theme: ReadingTheme = ReaderThemes.themes[0],
    val autoScroll: Boolean = false,
    val autoScrollSpeed: Float = 1f,
    val showReadingStats: Boolean = true,
    val enableGestures: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImmersiveReaderTopBar(
    progress: Float,
    onSettingsClick: () -> Unit,
    onLanguageSelect: (String) -> Unit,
    currentLanguage: String,
    isSpeaking: Boolean,
    onTtsClick: () -> Unit,
    isPdfLoading: Boolean,
    onPdfClick: () -> Unit,
) {
    var showLanguageMenu by remember { mutableStateOf(false) }
    val supportedLanguages = remember {
        mapOf(
            "en" to "English",
            "fr" to "French",
            "es" to "Spanish",
            "de" to "German",
            "it" to "Italian",
            "ar" to "Arabic"
        )
    }

    TopAppBar(
        title = { Text("Reader") },
        actions = {
            // TTS Button
            IconButton(
                onClick = onTtsClick,
            ) {
                Icon(
                    imageVector =
                    if (isSpeaking) Icons.Default.Pause
                    else Icons.Default.PlayArrow,
                    contentDescription = if (isSpeaking) "Stop TTS" else "Start TTS"
                )
            }

            // PDF Download Button
            IconButton(onClick = onPdfClick, enabled = !isPdfLoading) {
                if (isPdfLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download PDF"
                    )
                }
            }

            // Translation Button
            IconButton(onClick = { showLanguageMenu = true }) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "Select Language"
                )
            }

            // Settings Button
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Reading Settings"
                )
            }

            DropdownMenu(
                expanded = showLanguageMenu,
                onDismissRequest = { showLanguageMenu = false }
            ) {
                supportedLanguages.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onLanguageSelect(code)
                            showLanguageMenu = false
                        },
                        leadingIcon = {
                            if (code == currentLanguage) {
                                Icon(Icons.Default.Check, null)
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun ReadingSettingsDialog(
    preferences: ReadingPreferences,
    onPreferencesChanged: (ReadingPreferences) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reading Settings") },
        text = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Font size slider
                Text("Font Size")
                Slider(
                    value = preferences.fontSize,
                    onValueChange = {
                        onPreferencesChanged(preferences.copy(fontSize = it))
                    },
                    valueRange = 12f..24f
                )

                // Line height slider
                Text("Line Height")
                Slider(
                    value = preferences.lineHeight,
                    onValueChange = {
                        onPreferencesChanged(preferences.copy(lineHeight = it))
                    },
                    valueRange = 1f..2f
                )

                // Theme switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dark Mode")
                    Switch(
                        checked = preferences.isDarkMode,
                        onCheckedChange = {
                            onPreferencesChanged(preferences.copy(isDarkMode = it))
                        }
                    )
                }

                // Add Auto-scroll settings
                Text("Auto-scroll")
                Switch(
                    checked = preferences.autoScroll,
                    onCheckedChange = {
                        onPreferencesChanged(preferences.copy(autoScroll = it))
                    }
                )

                if (preferences.autoScroll) {
                    Text("Scroll Speed")
                    Slider(
                        value = preferences.autoScrollSpeed,
                        onValueChange = {
                            onPreferencesChanged(preferences.copy(autoScrollSpeed = it))
                        },
                        valueRange = 0.5f..3f
                    )
                }

                // Add gesture control toggle
                Text("Enable Gestures")
                Switch(
                    checked = preferences.enableGestures,
                    onCheckedChange = {
                        onPreferencesChanged(preferences.copy(enableGestures = it))
                    }
                )

                // Add reading stats toggle
                Text("Show Reading Stats")
                Switch(
                    checked = preferences.showReadingStats,
                    onCheckedChange = {
                        onPreferencesChanged(preferences.copy(showReadingStats = it))
                    }
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
private fun calculateWordsPerPage(content: String, fontSize: Int = 16): Int {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val lineHeight = 24.sp
    val paddingTop = 16.dp
    val paddingBottom = 16.dp
    val navigationHeight = 56.dp
    val progressIndicatorHeight = 48.dp

    val availableHeight =
        screenHeight - paddingTop - paddingBottom - navigationHeight - progressIndicatorHeight

    val linesPerPage = with(density) { (availableHeight.toPx() / lineHeight.toPx()).toInt() }

    // Estimate average characters per line based on screen width and font size
    val screenWidth = configuration.screenWidthDp.dp
    val averageCharWidth =
        with(density) { fontSize.dp.toPx() * 0.6f } // Approximate character width
    val charsPerLine = with(density) { (screenWidth.toPx() / averageCharWidth).toInt() }

    // Estimate words per line (average word length of 5 characters plus space)
    val averageWordsPerLine = charsPerLine / 6

    return linesPerPage * averageWordsPerLine
}

@Composable
fun HighlightedText(
    text: String,
    currentWordIndex: Int,
    fontSize: TextUnit = 16.sp,
    lineHeight: TextUnit = 24.sp,
    textColor: Color = Color.Black
) {
    val words = text.split(Regex("(?<=\\s)|(?=\\s)"))
    var currentIndex = 0

    Text(
        text =
        buildAnnotatedString {
            words.forEach { word ->
                val style =
                    if (currentIndex == currentWordIndex) {
                        SpanStyle(
                            background =
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.3f
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        SpanStyle(color = textColor)
                    }
                withStyle(style) { append(word) }
                if (!word.isBlank()) currentIndex++
            }
        },
        modifier = Modifier.verticalScroll(rememberScrollState()),
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = textColor
    )
}

class TranslationManager(private val context: Context) {
    private val translators = mutableMapOf<String, com.google.mlkit.nl.translate.Translator>()
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress = _downloadProgress.asStateFlow()

    fun getTranslator(targetLanguage: String): com.google.mlkit.nl.translate.Translator {
        return translators.getOrPut(targetLanguage) {
            val options =
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(targetLanguage)
                    .build()
            Translation.getClient(options)
        }
    }

    suspend fun translateText(text: String, targetLanguage: String): String {
        val translator = getTranslator(targetLanguage)

        try {
            // Check if model needs downloading
            val conditions = DownloadConditions.Builder().requireWifi().build()

            try {
                _downloadProgress.value = 0.1f
                translator.downloadModelIfNeeded(conditions).await()
                _downloadProgress.value = 0.5f
            } catch (e: Exception) {
                throw Exception("Failed to download language model: ${e.message}")
            }

            // Break text into smaller chunks for translation
            val chunks = text.split(". ").filter { it.isNotBlank() }
            val translatedChunks =
                chunks.mapIndexed { index, chunk ->
                    _downloadProgress.value = 0.5f + (0.5f * index / chunks.size)
                    translator.translate("$chunk.").await()
                }

            _downloadProgress.value = 1f
            return translatedChunks.joinToString(" ")
        } catch (e: Exception) {
            throw Exception("Translation failed: ${e.message}")
        }
    }

    fun cleanup() {
        translators.values.forEach { it.close() }
        translators.clear()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EPubReaderScreen(bookUrl: String) {
    var content by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableIntStateOf(0) }
    var pages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isPdfLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var epubContent by remember { mutableStateOf<String>("") }
    val wordsPerPage = calculateWordsPerPage(epubContent)
    val scope = rememberCoroutineScope()
    rememberScrollState()
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var pdfProgress by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    val viewModel: EPubReaderViewModel =
        viewModel(factory = EPubReaderViewModel.Factory(bookUrl, context))
    val progress by viewModel.readingProgress.collectAsState()
    val isSpeaking by viewModel.isSpeaking
    val currentWordIndex by viewModel.currentWordIndex

    val gradientColors =
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface
        )

    var showSettings by remember { mutableStateOf(false) }
    var preferences by remember { mutableStateOf(ReadingPreferences(isDarkMode = true)) }
    var isTranslating by remember { mutableStateOf(false) }
    var translatedContent by remember { mutableStateOf<String?>(null) }

    val translationManager = remember { TranslationManager(context) }
    var translationError by remember { mutableStateOf<String?>(null) }
    val translationProgress by translationManager.downloadProgress.collectAsState()

    // Translation function
    suspend fun translateContent(text: String) {
        isTranslating = true
        translationError = null
        try {
            translatedContent = translationManager.translateText(text, preferences.targetLanguage)
        } catch (e: Exception) {
            translationError = e.message
        } finally {
            isTranslating = false
        }
    }

    val currentContent = translatedContent ?: content

    LaunchedEffect(progress, pages) {
        if (pages.isNotEmpty() && progress != null) {
            currentPage = progress?.lastReadPage ?: 0
        }
    }

    LaunchedEffect(currentPage) {
        content = pages.getOrNull(currentPage)
        if (pages.isNotEmpty()) {
            viewModel.updateReadingProgress(currentPage, pages.size)
        }
    }

    LaunchedEffect(bookUrl) {
        scope.launch {
            try {
                downloadProgress = 0f
                epubContent =
                    withContext(Dispatchers.IO) {
                        downloadAndParseEpub(bookUrl) { progress ->
                            downloadProgress = progress
                        }
                    }
                // Recalculate pages with dynamic wordsPerPage
                pages = epubContent.split(" ").chunked(wordsPerPage).map { it.joinToString(" ") }

                // Initialize progress if this is the first time loading the book
                if (progress == null) {
                    viewModel.initializeProgress(pages.size)
                } else {
                    currentPage = progress?.lastReadPage ?: 0
                }

                content = pages.getOrNull(currentPage)
                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }
    }

    LaunchedEffect(currentPage) { content = pages.getOrNull(currentPage) }

    LaunchedEffect(Unit) {
        viewModel.setOnPageComplete { _ ->
            if (currentPage < pages.size - 1) {
                currentPage++
                // Get next page content
                pages.getOrNull(currentPage)?.let { nextPageContent ->
                    // First translate the content if needed
                    if (preferences.targetLanguage != "en") {
                        scope.launch {
                            try {
                                val translatedNextPage =
                                    translationManager.translateText(
                                        nextPageContent,
                                        preferences.targetLanguage
                                    )
                                viewModel.startSpeaking(translatedNextPage)
                            } catch (e: Exception) {
                                // Fallback to original content if translation fails
                                viewModel.startSpeaking(nextPageContent)
                            }
                        }
                    } else {
                        viewModel.startSpeaking(nextPageContent)
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) { onDispose { translationManager.cleanup() } }

    fun updateTtsLanguage(languageCode: String) {
        viewModel.pauseSpeaking() // Stop current speech
        viewModel.updateTtsLanguage(languageToLocale[languageCode] ?: Locale.ENGLISH)
    }

    var showThemeSelector by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }

    val activity = LocalContext.current as Activity
    val voiceCommandHandler = remember { VoiceCommandHandler(activity) }
    val isListeningForCommands by voiceCommandHandler.isListening.collectAsState()

    DisposableEffect(Unit) { onDispose { voiceCommandHandler.destroy() } }

    fun handleVoiceCommand(command: VoiceCommand) {
        when (command) {
            VoiceCommand.START -> {
                if (!isSpeaking) {
                    (translatedContent ?: content)?.let { textToSpeak ->
                        viewModel.startSpeaking(textToSpeak)
                    }
                }
            }

            VoiceCommand.STOP -> {
                if (isSpeaking) {
                    viewModel.pauseSpeaking()
                }
            }

            VoiceCommand.NEXT -> {
                if (currentPage < pages.size - 1) {
                    currentPage++
                }
            }

            VoiceCommand.PREVIOUS -> {
                if (currentPage > 0) {
                    currentPage--
                }
            }
        }
    }

    // Add periodic stats update
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000) // Update every 30 seconds
            viewModel.updateReadingStats(content, currentPage)
        }
    }

    // Add cleanup
    DisposableEffect(Unit) {
        onDispose {
            viewModel.endReadingSession()
            voiceCommandHandler.destroy()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ImmersiveReaderTopBar(
                progress = currentPage.toFloat() / pages.size,
                onSettingsClick = { showSettings = true },
                onLanguageSelect = { newLang ->
                    preferences = preferences.copy(targetLanguage = newLang)
                    updateTtsLanguage(newLang) // Add this line
                    scope.launch { content?.let { translateContent(it) } }
                },
                currentLanguage = preferences.targetLanguage,
                isSpeaking = isSpeaking,
                onTtsClick = {
                    if (isSpeaking) {
                        viewModel.pauseSpeaking()
                    } else {
                        // Use translatedContent if available, otherwise use original
                        // content
                        (translatedContent ?: content)?.let { textToSpeak ->
                            viewModel.startSpeaking(textToSpeak)
                        }
                    }
                },
                isPdfLoading = isPdfLoading,
                onPdfClick = {
                    scope.launch {
                        isPdfLoading = true
                        pdfProgress = 0f
                        try {
                            createAndOpenPdf(epubContent, context) { progress ->
                                pdfProgress = progress
                            }
                            snackbarHostState.showSnackbar("PDF saved and opened")
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Error: ${e.message}")
                        } finally {
                            isPdfLoading = false
                            pdfProgress = 0f
                        }
                    }
                }
            )
        },
        bottomBar = {
            ReaderBottomBar(
                preferences = preferences,
                onAutoScrollToggle = { enabled ->
                    preferences = preferences.copy(autoScroll = enabled)
                    viewModel.toggleAutoScroll(enabled, preferences.autoScrollSpeed)
                },
                onSpeedChange = { speed ->
                    preferences = preferences.copy(autoScrollSpeed = speed)
                    if (preferences.autoScroll) {
                        viewModel.toggleAutoScroll(true, speed)
                    }
                },
                onBookmarkClick = { showBookmarks = true },
                onThemeClick = { showThemeSelector = true },
                onStatsClick = { showStats = true },
                onVoiceCommandClick = {
                    if (isListeningForCommands) {
                        voiceCommandHandler.stopListening()
                    } else {
                        voiceCommandHandler.startListening { command ->
                            handleVoiceCommand(command)
                        }
                    }
                },
                isListeningForCommands = isListeningForCommands
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    content?.let { currentText ->
                        viewModel.addBookmark(currentPage, currentText.take(50) + "...")
                    }
                }
            ) { Icon(Icons.Default.BookmarkAdd, "Add Bookmark") }
        }
    ) { paddingValues ->
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(gradientColors))
        ) {
            when {
                isLoading -> {
                    LoadingScreen(downloadProgress)
                }

                error != null -> {
                    ErrorScreen(error!!)
                }

                currentContent != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Reading Progress Arc
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = currentPage.toFloat() / pages.size.coerceAtLeast(1),
                                modifier = Modifier.size(60.dp),
                                strokeWidth = 4.dp
                            )
                        }

                        if (isTranslating) {
                            LinearProgressIndicator(
                                progress = translationProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Translating... ${(translationProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        translationError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Content Card
                        Card(
                            modifier =
                            Modifier
                                .weight(1f)
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .pointerInput(Unit) {
                                    if (preferences.enableGestures) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            if (abs(dragAmount.x) >
                                                abs(dragAmount.y)
                                            ) {
                                                if (dragAmount.x > 0 &&
                                                    currentPage > 0
                                                ) {
                                                    scope.launch { currentPage-- }
                                                } else if (dragAmount.x < 0 &&
                                                    currentPage <
                                                    pages.size -
                                                    1
                                                ) {
                                                    scope.launch { currentPage++ }
                                                }
                                            }
                                        }
                                    }
                                }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Background ambient effect based on theme
                                Box(
                                    modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                colors =
                                                listOf(
                                                    preferences
                                                        .theme
                                                        .backgroundColor,
                                                    preferences
                                                        .theme
                                                        .backgroundColor
                                                        .copy(
                                                            alpha =
                                                            0.8f
                                                        )
                                                )
                                            )
                                        )
                                )

                                // Content
                                if (isTranslating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                } else {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        if (preferences.showReadingStats) {
                                            ReadingStatsOverlay(
                                                statistics = viewModel.readingStats.value,
                                                modifier = Modifier.padding(bottom = 16.dp)
                                            )
                                        }

                                        HighlightedText(
                                            text = currentContent!!,
                                            currentWordIndex = currentWordIndex,
                                            fontSize = preferences.fontSize.sp,
                                            lineHeight = preferences.lineHeight.em,
                                            textColor = preferences.theme.textColor
                                        )
                                    }
                                }

                                // Auto-scroll progress indicator
                                if (preferences.autoScroll) {
                                    LinearProgressIndicator(
                                        progress = viewModel.autoScrollProgress.value,
                                        modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        }

                        // Navigation Controls
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            NavigationControls(
                                currentPage = currentPage,
                                totalPages = pages.size,
                                onPrevious = { if (currentPage > 0) currentPage-- },
                                onNext = { if (currentPage < pages.size - 1) currentPage++ }
                            )
                        }
                    }
                }
            }
        }
    }

    // Show settings dialog
    if (showSettings) {
        ReadingSettingsDialog(
            preferences = preferences,
            onPreferencesChanged = { preferences = it },
            onDismiss = { showSettings = false }
        )
    }

    // Add dialogs
    if (showThemeSelector) {
        ThemeSelector(
            currentTheme = preferences.theme,
            onThemeSelect = { theme ->
                preferences = preferences.copy(theme = theme)
                showThemeSelector = false
            },
            onDismiss = { showThemeSelector = false }
        )
    }

    if (showBookmarks) {
        AlertDialog(
            onDismissRequest = { showBookmarks = false },
            title = { Text("Bookmarks") },
            text = {
                BookmarksList(
                    bookmarks = viewModel.bookmarks.value,
                    onBookmarkClick = { page ->
                        currentPage = page
                        showBookmarks = false
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showBookmarks = false }) { Text("Close") }
            }
        )
    }

    if (showStats) {
        AlertDialog(
            onDismissRequest = { showStats = false },
            title = { Text("Reading Statistics") },
            text = {
                ReadingStatsOverlay(
                    statistics = viewModel.readingStats.value,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = { TextButton(onClick = { showStats = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun LoadingScreen(progress: Float) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(80.dp), strokeWidth = 8.dp)
        if (progress > 0f) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ErrorScreen(errorMessage: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Error",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun NavigationControls(
    currentPage: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = onPrevious,
            enabled = currentPage > 0,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Previous") }

        Text(
            "${currentPage + 1} / $totalPages",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleMedium
        )

        FilledTonalButton(
            onClick = onNext,
            enabled = currentPage < totalPages - 1,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Next") }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private suspend fun downloadAndParseEpub(epubUrl: String, onProgress: (Float) -> Unit): String {
    return withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("book", ".epub")
        val client = OkHttpClient()

        val request = Request.Builder().url(epubUrl).build()

        client.newCall(request).execute().use { response ->
            val body = response.body
            val contentLength = body?.contentLength() ?: -1L
            val input = BufferedInputStream(body?.byteStream())

            tempFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                        withContext(Dispatchers.Main) { onProgress(progress) }
                    }
                }
            }
        }

        // Rest of the parsing code remains the same
        val xhtmlContent = StringBuilder()
        ZipFile(tempFile).use { zip ->
            zip.entries().asIterator().forEach { entry ->
                if (entry.name.endsWith(".xhtml")) {
                    zip.getInputStream(entry).use { stream ->
                        InputStreamReader(stream).use { reader ->
                            reader.readLines().forEach { line -> xhtmlContent.append(line) }
                        }
                    }
                }
            }
        }

        tempFile.delete()

        val doc = Jsoup.parse(xhtmlContent.toString(), "", Parser.xmlParser())
        doc.text()
    }
}

private suspend fun createAndOpenPdf(
    content: String,
    context: Context,
    onProgress: (Float) -> Unit
): File {
    return withContext(Dispatchers.IO) {
        val fileName = "book_${System.currentTimeMillis()}.pdf"
        val contentValues =
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

        val uri =
            context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )
                ?: throw IllegalStateException("Failed to create PDF file")

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            Document().apply {
                PdfWriter.getInstance(this, outputStream)
                open()

                // Split content into chunks for progress tracking
                val chunks = content.chunked(1000)
                chunks.forEachIndexed { index, chunk ->
                    add(Paragraph(chunk))
                    withContext(Dispatchers.Main) {
                        onProgress((index + 1).toFloat() / chunks.size)
                    }
                }
                close()
            }
        }

        // Open PDF viewer
        try {
            val intent =
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            val chooserIntent = Intent.createChooser(intent, "Open PDF with...")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            val marketIntent =
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://search?q=pdf+viewer")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.startActivity(marketIntent)
        }

        File(uri.path!!)
    }
}

@Composable
private fun ReadingStatsOverlay(statistics: ReadingStatistics, modifier: Modifier = Modifier) {
    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(16.dp)
    ) {
        Text(
            "Reading Time: ${formatDuration(statistics.timeSpentReading)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text("Pages Read: ${statistics.pagesRead}", style = MaterialTheme.typography.bodyMedium)
        Text(
            "Reading Speed: ${statistics.averageReadingSpeed.roundToInt()} words/min",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun BookmarksList(
    bookmarks: List<BookmarkPage>,
    onBookmarkClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(bookmarks.size) { bookmark ->
            ListItem(
                headlineContent = { Text("Page ${bookmarks[bookmark].pageNumber + 1}") },
                supportingContent = { Text(bookmarks[bookmark].snippet) },
                leadingContent = { Icon(Icons.Default.Bookmark, null) },
                modifier =
                Modifier.clickable {
                    onBookmarkClick(bookmarks[bookmark].pageNumber)
                }
            )
        }
    }
}

@Composable
private fun ReaderBottomBar(
    preferences: ReadingPreferences,
    onAutoScrollToggle: (Boolean) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onBookmarkClick: () -> Unit,
    onThemeClick: () -> Unit,
    onStatsClick: () -> Unit,
    onVoiceCommandClick: () -> Unit,
    isListeningForCommands: Boolean
) {
    BottomAppBar(
        actions = {
            IconButton(onClick = onBookmarkClick) { Icon(Icons.Default.Bookmark, "Bookmarks") }
            IconButton(onClick = { onAutoScrollToggle(!preferences.autoScroll) }) {
                Icon(
                    if (preferences.autoScroll) Icons.Default.Pause
                    else Icons.Default.PlayArrow,
                    "Auto-scroll"
                )
            }
            if (preferences.autoScroll) {
                Slider(
                    value = preferences.autoScrollSpeed,
                    onValueChange = onSpeedChange,
                    valueRange = 0.5f..3f,
                    modifier = Modifier.width(100.dp)
                )
            }
            IconButton(onClick = onThemeClick) { Icon(Icons.Default.Palette, "Themes") }
            IconButton(onClick = onStatsClick) { Icon(Icons.Default.Timeline, "Reading Stats") }
            IconButton(onClick = onVoiceCommandClick) {
                Icon(
                    imageVector =
                    if (isListeningForCommands) Icons.Default.Mic
                    else Icons.Default.MicNone,
                    contentDescription =
                    if (isListeningForCommands) "Stop voice commands"
                    else "Start voice commands",
                    tint =
                    if (isListeningForCommands) MaterialTheme.colorScheme.primary
                    else LocalContentColor.current
                )
            }
        }
    )
}

@Composable
private fun ThemeSelector(
    currentTheme: ReadingTheme,
    onThemeSelect: (ReadingTheme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            LazyColumn {
                items(ReaderThemes.themes.size) { theme ->
                    Row(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onThemeSelect(ReaderThemes.themes.get(theme))
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ReaderThemes.themes.get(theme).name)
                        if (ReaderThemes.themes.get(theme) == currentTheme) {
                            Icon(Icons.Default.Check, null)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

@Preview(showBackground = true)
@Composable
fun EPubReaderScreenPreview() {
    EPubReaderScreen("https://www.gutenberg.org/cache/epub/1513/pg1513.txt")
}
