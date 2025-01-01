package tn.esprit.libraryapp.utils

import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.MutableStateFlow
import tn.esprit.libraryapp.models.LibrarianAction
import tn.esprit.libraryapp.models.LibrarianContext
import tn.esprit.libraryapp.models.LibrarianMode
import tn.esprit.libraryapp.models.LibrarianState
import tn.esprit.libraryapp.services.MLKitService

class LibrarianManager(
    private val navController: NavHostController,
    private val mlKitService: MLKitService
) {
    val state = MutableStateFlow(LibrarianState())
    private var conversationContext = mutableListOf<String>()
    private var lastAction: LibrarianAction? = null
    private var context = LibrarianContext()

    suspend fun handleAction(action: LibrarianAction) {
        when (action) {
            is LibrarianAction.ProcessBookCover -> handleBookCover(action)
            is LibrarianAction.Chat -> handleChat(action)
            is LibrarianAction.VoiceSearch -> handleVoiceSearch(action)
            is LibrarianAction.TranslateText -> handleTranslation(action)
            is LibrarianAction.DetectFaces -> handleFaceDetection(action)
            is LibrarianAction.Dismiss -> handleDismiss()
            is LibrarianAction.GetSmartReply -> handleSmartReply(action)
            is LibrarianAction.Help -> handleHelp()
            is LibrarianAction.ImageLabel -> handleImageLabel(action)
            is LibrarianAction.ReadBook -> handleReadBook(action)
            is LibrarianAction.RecognizeText -> handleTextRecognition(action)
            is LibrarianAction.Recommend -> handleRecommend(action)
            is LibrarianAction.ScanBarcode -> handleBarcodeScanning(action)
            is LibrarianAction.ScanText -> handleTextScanning(action)
            is LibrarianAction.Search -> handleSearch(action)
            is LibrarianAction.SmartRecommend -> handleSmartRecommend(action)
            is LibrarianAction.ViewDetails -> handleViewDetails(action)
            LibrarianAction.Welcome -> handleWelcome()
            is LibrarianAction.Navigation -> handleNavigation(action)
        }
    }

    private suspend fun handleBookCover(action: LibrarianAction.ProcessBookCover) {
        updateState { copy(isTyping = true, message = "Analyzing book cover...") }

        action.image?.let { image ->
            val labels = mlKitService.labelImage(image)
            val text = mlKitService.recognizeText(image)
            val barcodes = mlKitService.scanBarcode(image)

            when {
                barcodes.isNotEmpty() -> {
                    val isbn = barcodes.first().rawValue.toString()
                    NavigationHandler.handleLibrarianNavigation(
                        navController,
                        "search",
                        mapOf("isbn" to isbn)
                    )
                }

                text != null -> {
                    NavigationHandler.handleLibrarianNavigation(
                        navController,
                        "search",
                        mapOf("query" to text.text)
                    )
                }

                labels.isNotEmpty() -> {
                    val bookRelatedLabels =
                        labels.filter {
                            it.text.contains("book", true) || it.text.contains("text", true)
                        }
                    if (bookRelatedLabels.isNotEmpty()) {
                        handleSearch(LibrarianAction.Search(bookRelatedLabels.first().text))
                    }
                }
            }

            updateState {
                copy(
                    isTyping = false,
                    message = "I found some interesting details about this book!",
                    suggestions = listOf("Search Results", "Try Another", "Help"),
                    currentMode = LibrarianMode.SCANNING
                )
            }
        }
    }

    private suspend fun handleChat(action: LibrarianAction.Chat) {
        conversationContext.add(action.message)

        when {
            action.message.startsWith("find", true) -> {
                val query = action.message.substringAfter("find").trim()
                handleSearch(LibrarianAction.Search(query))
            }

            action.message.startsWith("scan", true) -> {
                updateState {
                    copy(
                        message = "Please show me the book cover",
                        currentMode = LibrarianMode.SCANNING
                    )
                }
            }

            action.message.startsWith("translate", true) -> {
                val text = action.message.substringAfter("translate").trim()
                handleTranslation(LibrarianAction.TranslateText(text, "en"))
            }

            action.message.startsWith("recommend", true) -> {
                val genre = action.message.substringAfter("recommend").trim()
                handleRecommend(LibrarianAction.Recommend(genre))
            }

            else -> {
                // Get smart reply suggestions
                val replies = mlKitService.getSmartReplies(conversationContext)
                updateState {
                    copy(
                        message = "I understand you're interested in: ${action.message}",
                        suggestions = replies,
                        currentMode = LibrarianMode.CHATTING
                    )
                }
            }
        }
    }

    private suspend fun handleVoiceSearch(action: LibrarianAction.VoiceSearch) {
        navController.navigate("search?query=${action.query}")
        updateState {
            copy(
                message = "Searching for: ${action.query}",
                suggestions = listOf("Filter Results", "Sort By", "Advanced Search")
            )
        }
    }

    private suspend fun handleTranslation(action: LibrarianAction.TranslateText) {
        val translated = mlKitService.translateText(action.text, action.targetLang)
        updateState {
            copy(
                message = "Translation: $translated",
                suggestions = listOf("Search This", "Share", "More Translations")
            )
        }
    }

    private fun handleNavigation(action: LibrarianAction.Navigation) {
        navController.navigate(action.route)
    }

    private suspend fun handleFaceDetection(action: LibrarianAction.DetectFaces) {
        updateState { copy(isTyping = true, message = "Processing faces...") }
        val faces = mlKitService.detectFaces(action.image)
        updateState {
            copy(
                isTyping = false,
                message = "Found ${faces.size} faces",
                suggestions = listOf("Save Image", "Share", "Delete")
            )
        }
    }

    private suspend fun handleDismiss() {
        updateState { copy(isVisible = false, message = "", suggestions = emptyList()) }
    }

    private suspend fun handleSmartReply(action: LibrarianAction.GetSmartReply) {
        val replies = mlKitService.getSmartReplies(action.conversation)
        updateState { copy(suggestions = replies) }
    }

    private suspend fun handleHelp() {
        updateState {
            copy(
                message = "How can I help you today?",
                suggestions =
                listOf("Search Books", "Scan Book", "Translate Text", "Voice Search")
            )
        }
    }

    private suspend fun handleImageLabel(action: LibrarianAction.ImageLabel) {
        val labels = mlKitService.labelImage(action.image)
        updateState {
            copy(
                message = "I see: ${labels.joinToString(", ") { it.text }}",
                suggestions = labels.take(3).map { it.text },
                imageLabels = labels
            )
        }
    }

    private suspend fun handleReadBook(action: LibrarianAction.ReadBook) {
        navController.navigate("reader/${action.bookId}")
    }

    private suspend fun handleTextRecognition(action: LibrarianAction.RecognizeText) {
        val text = mlKitService.recognizeText(action.image)
        updateState {
            copy(
                message = "Recognized text: ${text?.text ?: ""}",
                suggestions = listOf("Search This", "Translate", "Copy")
            )
        }
    }

    private suspend fun handleSmartRecommend(action: LibrarianAction.SmartRecommend) {
        context = context.copy(userPreferences = action.preferences.mapValues { it.value.toInt() })

        val recommendations =
            action.preferences.entries.sortedByDescending { it.value }.take(3).map { it.key }

        updateState {
            copy(
                message = "Based on your interests, you might like:",
                suggestions = recommendations,
                currentMode = LibrarianMode.SMART_RECOMMEND
            )
        }

        NavigationHandler.handleLibrarianNavigation(
            navController,
            "recommendations",
            mapOf("genres" to recommendations.joinToString(","))
        )
    }

    private suspend fun handleViewDetails(action: LibrarianAction.ViewDetails) {
        navController.navigate("book/${action.bookId}")
    }

    private suspend fun handleWelcome() {
        updateState {
            copy(
                isVisible = true,
                message = "Hello! How can I assist you today?",
                suggestions = listOf("Search Books", "Scan Book", "Help")
            )
        }
    }

    private suspend fun handleSearch(action: LibrarianAction.Search) {
        updateState { copy(isTyping = true, message = "Searching for: ${action.query}") }

        NavigationHandler.handleLibrarianNavigation(
            navController,
            "search",
            mapOf("query" to action.query)
        )

        val smartReplies =
            mlKitService.getSmartReplies(listOf("Show me books about ${action.query}"))

        updateState {
            copy(
                isTyping = false,
                suggestions =
                smartReplies + listOf("Filter Results", "Sort By", "Scan Book Instead"),
                currentMode = LibrarianMode.SEARCHING
            )
        }
    }

    private suspend fun handleRecommend(action: LibrarianAction.Recommend) {
        val genre = action.genre ?: "general"
        context = context.copy(lastGenre = genre)

        updateState {
            copy(
                message = "Looking for $genre books...",
                isTyping = true,
                currentMode = LibrarianMode.RECOMMENDING
            )
        }

        NavigationHandler.handleLibrarianNavigation(
            navController,
            "recommendations",
            mapOf("genre" to genre)
        )

        updateState {
            copy(
                isTyping = false,
                message = "Here are some $genre books you might like",
                suggestions = listOf("More Like This", "Different Genre", "Popular in $genre")
            )
        }
    }

    private suspend fun handleBarcodeScanning(action: LibrarianAction.ScanBarcode) {
        updateState {
            copy(
                isTyping = true,
                message = "Scanning barcode...",
                currentMode = LibrarianMode.BARCODE_SCANNING
            )
        }

        val barcodes = mlKitService.scanBarcode(action.image)
        val isbn = barcodes.firstOrNull()?.rawValue

        if (isbn != null) {
            NavigationHandler.handleLibrarianNavigation(
                navController,
                "search",
                mapOf("isbn" to isbn)
            )

            updateState {
                copy(
                    isTyping = false,
                    message = "Found book with ISBN: $isbn",
                    suggestions = listOf("View Details", "Search Similar", "Scan Another"),
                    detectedBarcode = isbn
                )
            }
        } else {
            updateState {
                copy(
                    isTyping = false,
                    message = "No barcode found. Try again?",
                    suggestions = listOf("Try Again", "Manual Search", "Help")
                )
            }
        }
    }

    private suspend fun handleTextScanning(action: LibrarianAction.ScanText) {
        updateState {
            copy(
                isTyping = true,
                message = "Scanning text...",
                currentMode = LibrarianMode.TEXT_RECOGNITION
            )
        }

        val text = mlKitService.recognizeText(action.image)

        if (text != null) {
            updateState {
                copy(
                    isTyping = false,
                    message = "I found some text! Would you like to:",
                    suggestions = listOf("Search This", "Translate", "Copy Text"),
                    recognizedText = text
                )
            }
        } else {
            updateState {
                copy(
                    isTyping = false,
                    message = "No text found. Try another image?",
                    suggestions = listOf("Try Again", "Manual Input", "Help")
                )
            }
        }
    }

    private suspend fun handleBookWithText(text: String) {
        NavigationHandler.handleLibrarianNavigation(navController, "search", mapOf("query" to text))

        updateState {
            copy(
                isTyping = false,
                message = "Searching for books with: $text",
                suggestions = listOf("Refine Search", "Filter Results", "More Options"),
                currentMode = LibrarianMode.SEARCHING
            )
        }
    }

    private suspend fun updateState(update: LibrarianState.() -> LibrarianState) {
        state.emit(update(state.value))
    }
}

object NavigationHandler {
    fun handleLibrarianNavigation(
        navController: NavHostController,
        route: String,
        params: Map<String, String> = emptyMap()
    ) {
        val fullRoute = buildRoute(route, params)
        navController.navigate(fullRoute) {
            launchSingleTop = true
            restoreState = true
        }
    }

    private fun buildRoute(baseRoute: String, params: Map<String, String>): String {
        if (params.isEmpty()) return baseRoute
        return buildString {
            append(baseRoute)
            append("?")
            params.entries.forEachIndexed { index, entry ->
                if (index > 0) append("&")
                append("${entry.key}=${entry.value}")
            }
        }
    }
}
