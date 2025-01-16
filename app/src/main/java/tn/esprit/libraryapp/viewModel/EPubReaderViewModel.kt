package tn.esprit.libraryapp.viewModel

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tn.esprit.libraryapp.database.AppDatabase
import tn.esprit.libraryapp.models.BookmarkPage
import tn.esprit.libraryapp.models.ReadingStatistics
import tn.esprit.libraryapp.repository.BookProgressRepository
import java.util.Locale

class EPubReaderViewModel(
    private val repository: BookProgressRepository,
    private val bookUrl: String,
    private val context: Context,
) : ViewModel() {

    val readingProgress =
        repository
            .getProgress(bookUrl)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var _isSpeaking = mutableStateOf(false)
    val isSpeaking: State<Boolean> = _isSpeaking

    private var _onPageComplete = mutableStateOf<((String?) -> Unit)?>(null)
    fun setOnPageComplete(callback: (String?) -> Unit) {
        _onPageComplete.value = callback
    }

    private var _currentWordIndex = mutableIntStateOf(-1)
    val currentWordIndex: State<Int> = _currentWordIndex

    fun setCurrentWordIndex(index: Int) {
        _currentWordIndex.value = index
    }

    private var _currentContent = mutableStateOf<String?>(null)
    private val content: String?
        get() = _currentContent.value

    private var _currentLocale = mutableStateOf(Locale.ENGLISH)
    val currentLocale: State<Locale> = _currentLocale

    fun updateTtsLanguage(locale: Locale) {
        textToSpeech?.let { tts ->
            tts.stop()
            _isSpeaking.value = false

            // Check if language is available
            val result = tts.isLanguageAvailable(locale)
            when (result) {
                TextToSpeech.LANG_AVAILABLE,
                TextToSpeech.LANG_COUNTRY_AVAILABLE,
                TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE,
                -> {
                    tts.language = locale
                    _currentLocale.value = locale
                    // Wait for language switch to complete
                    tts.setOnUtteranceProgressListener(
                        object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {}
                            override fun onDone(utteranceId: String?) {}
                            override fun onError(utteranceId: String?) {}
                            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                                super.onStop(utteranceId, interrupted)
                                // Reset to original listener after language switch
                                setupUtteranceProgressListener()
                            }
                        },
                    )
                }

                TextToSpeech.LANG_MISSING_DATA -> {
                    // Prompt to install language data
                    val intent =
                        Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    context.startActivity(intent)
                }

                TextToSpeech.LANG_NOT_SUPPORTED -> {
                    _isSpeaking.value = false
                }
            }
        }
    }

    private fun setupUtteranceProgressListener() {
        textToSpeech?.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _currentWordIndex.value = 0
                }

                override fun onError(utteranceId: String?) {
                    _currentWordIndex.value = -1
                }

                override fun onDone(utteranceId: String?) {
                    _currentWordIndex.value = -1
                    _onPageComplete.value?.invoke(utteranceId)
                }

                override fun onRangeStart(
                    utteranceId: String?,
                    start: Int,
                    end: Int,
                    frame: Int,
                ) {
                    _currentWordIndex.value = countWordsUpTo(start)
                }
            },
        )
    }

    fun startSpeaking(text: String, utteranceId: String? = null) {
        if (isTtsInitialized) {
            _currentContent.value = text
            val params =
                Bundle().apply {
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                    putFloat(TextToSpeech.Engine.KEY_PARAM_PAN, 0.0f)
                }
            val id = utteranceId ?: "PAGE_${System.currentTimeMillis()}"

            textToSpeech?.let { tts ->
                // Ensure the language is properly set before speaking
                if (tts.isLanguageAvailable(tts.voice?.locale ?: Locale.ENGLISH) >=
                    TextToSpeech.LANG_AVAILABLE
                ) {
                    // Add a small delay to ensure language switch is complete
                    viewModelScope.launch {
                        delay(100)
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, id)
                        _isSpeaking.value = true
                    }
                }
            }
        }
    }

    init {
        textToSpeech =
            TextToSpeech(context) { status ->
                isTtsInitialized = status == TextToSpeech.SUCCESS
                if (isTtsInitialized) {
                    // Start with system default locale
                    val systemLocale =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            context.resources.configuration.locales[0]
                        } else {
                            @Suppress("DEPRECATION")
                            context.resources.configuration.locale
                        }
                    updateTtsLanguage(systemLocale)

                    setupUtteranceProgressListener()
                }
            }
        startReadingSession()
    }

    private fun countWordsUpTo(charIndex: Int): Int {
        return content?.substring(0, charIndex)?.count { it.isWhitespace() } ?: 0
    }

    fun pauseSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
        _currentContent.value = null // Clear content when stopping
    }

    override fun onCleared() {
        endReadingSession()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onCleared()
    }

    fun updateReadingProgress(currentPage: Int, totalPages: Int) {
        viewModelScope.launch {
            repository.updateProgress(bookUrl, currentPage, totalPages)
            // Update stats when page changes
            updateReadingStats(
                content,
                currentPage = currentPage,
            )
        }
    }

    fun initializeProgress(totalPages: Int) {
        viewModelScope.launch { repository.saveInitialProgress(bookUrl, totalPages) }
    }

    private var _readingStats = mutableStateOf(ReadingStatistics())
    val readingStats: State<ReadingStatistics> = _readingStats

    private var _bookmarks = mutableStateOf<List<BookmarkPage>>(emptyList())
    val bookmarks: State<List<BookmarkPage>> = _bookmarks

    private var _autoScrollProgress = mutableFloatStateOf(0f)
    val autoScrollProgress: State<Float> = _autoScrollProgress

    private var readingStartTime = System.currentTimeMillis()
    private var lastUpdateTime = System.currentTimeMillis()
    private var totalWordsRead = 0
    private var sessionTimeRead = 0L
    private var autoScrollJob: Job? = null

    fun toggleAutoScroll(enabled: Boolean, speed: Float) {
        autoScrollJob?.cancel()
        if (enabled) {
            autoScrollJob =
                viewModelScope.launch {
                    while (isActive) {
                        delay((1000 / speed).toLong())
                        _autoScrollProgress.value += 0.001f
                        if (_autoScrollProgress.value >= 1f) {
                            _autoScrollProgress.value = 0f
                            // Move to next page
                            _onPageComplete.value?.invoke(null)
                        }
                    }
                }
        }
    }

    fun addBookmark(pageNumber: Int, snippet: String) {
        val bookmark = BookmarkPage(pageNumber, snippet)
        _bookmarks.value = _bookmarks.value + bookmark
    }

    fun removeBookmark(pageNumber: Int) {
        // _bookmarks.value = _bookmarks.value.filterNot { Bookmark.pageNumber == pageNumber }
    }

    fun updateReadingStats(currentPageText: String?, currentPage: Int) {
        val currentTime = System.currentTimeMillis()
        val timeElapsed = currentTime - lastUpdateTime
        sessionTimeRead += timeElapsed

        currentPageText?.let { totalWordsRead += it.split(Regex("\\s+")).size }

        val minutesElapsed = sessionTimeRead / 60000f // Convert to minutes
        val wordsPerMinute = if (minutesElapsed > 0) totalWordsRead / minutesElapsed else 0f

        _readingStats.value =
            _readingStats.value.copy(
                timeSpentReading = sessionTimeRead,
                pagesRead = currentPage + 1,
                bookmarksCount = _bookmarks.value.size,
                averageReadingSpeed = wordsPerMinute,
                lastReadTimestamp = currentTime,
            )

        lastUpdateTime = currentTime
    }

    // Start tracking when reading session begins
    fun startReadingSession() {
        readingStartTime = System.currentTimeMillis()
        lastUpdateTime = readingStartTime
        sessionTimeRead = 0L
        totalWordsRead = 0
    }

    // Stop tracking when session ends
    fun endReadingSession() {
        updateReadingStats(
            content,
            currentPage = 0,
        )
    }

    class Factory(private val bookUrl: String, private val context: Context) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val database = AppDatabase.getDatabase(context)
            val repository = BookProgressRepository(database.bookProgressDao())
            return EPubReaderViewModel(repository, bookUrl, context) as T
        }
    }
}
