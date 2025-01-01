package tn.esprit.libraryapp.screens

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.repository.BookRepository

@Preview(showBackground = true)
@Composable
fun BookChatScreen(modifier: Modifier = Modifier) {
    val applicationContext = LocalContext.current
    val offlinePluginFactory = StreamOfflinePluginFactory(appContext = applicationContext)
    val statePluginFactory =
        StreamStatePluginFactory(config = StatePluginConfig(), appContext = applicationContext)
    val client =
        ChatClient.Builder("r2mnrp6eqtza", applicationContext)
            .withPlugins(offlinePluginFactory, statePluginFactory)
            .logLevel(ChatLogLevel.ALL)
            .build()
    val user =
        User(id = "675cf18029f39ca8a5fb636a", name = "Ali", image = "https://bit.ly/2TIt8NR")
    val token = client.devToken(user.id)
    client.connectUser(user = user, token = token).execute()
    ChatTheme {
        val clientInitializationState by client.clientState.initializationState.collectAsState()
        when (clientInitializationState) {
            InitializationState.COMPLETE -> {
                ChatChannelScreen(client = client)
            }

            InitializationState.INITIALIZING -> {
                Text(text = "Initializing...")
            }

            InitializationState.NOT_INITIALIZED -> {
                Text(text = "Not initialized...")
            }

            else -> {
                Text(text = "Unknown state")
            }
        }
    }
}

class CustomChannelListViewModel(private val chatClient: ChatClient = ChatClient.instance()) :
    ViewModel() {
    private val repository = BookRepository()
    private val _uiState = MutableStateFlow(ChannelListUiState())
    val uiState = _uiState.asStateFlow()

    private val _bookmarkedBooks = MutableStateFlow<List<Book>>(emptyList())
    val bookmarkedBooks: StateFlow<List<Book>> = _bookmarkedBooks

    data class ChannelListUiState(
        val channels: List<Channel> = emptyList(),
        val error: String? = null,
    )

    init {
        fetchBookmarkedBooks()
        setupChannelsFromBookmarks()
    }

    private fun setupChannelsFromBookmarks() {
        viewModelScope.launch {
            bookmarkedBooks.collect { books ->
                val channels =
                    books.map { book ->
                        Channel(
                            id = book.id.toString(),
                            name = book.title,
                            image = book.coverImage ?: "",
                        )
                    }
                _uiState.update { it.copy(channels = channels, error = null) }
            }
        }
    }

    fun fetchBookmarkedBooks() {
        viewModelScope.launch {
            try {
                // val userId = TokenManagerProvider.getInstance().userId.first()
                val userId = "675cf18029f39ca8a5fb636a"
                Log.d("BookChannelViewModel", "User ID: $userId")
                if (userId != null) {
                    val bookmarks = repository.getBookmarks(userId)
                    _bookmarkedBooks.value = bookmarks
                }
            } catch (e: Exception) {
                Log.e("BookChannelViewModel", "Error fetching bookmarks", e)
                _bookmarkedBooks.value = emptyList()
            }
        }
    }
}

@Composable
fun ChatChannelScreen(
    viewModel: CustomChannelListViewModel = viewModel(),
    client: ChatClient = ChatClient.instance(),
) {
    var selectedChannel by remember { mutableStateOf<Channel?>(null) }
    val context = LocalContext.current

    if (selectedChannel != null) {
        MessagesScreen(
            viewModelFactory = MessagesViewModelFactory(
                context = context,
                channelId = selectedChannel!!.cid,
                messageLimit = 30
            ),
            onBackPressed = { selectedChannel = null }
        )
    } else {
        val bookmarkedBooks by viewModel.bookmarkedBooks.collectAsState()
        // Create channels from bookmarked books
        bookmarkedBooks.forEach { book ->
            Log.d("MainActivity", "Creating channel for book: ${book.title}")
            val channelClient =
                client.channel(channelType = "messaging", channelId = "book-${book.id}")

            channelClient.create(
                memberIds = listOf("tutorial-droid"),
                extraData =
                mutableMapOf(
                    "name" to book.title,
                    "image" to (book.coverImage ?: ""),
                    "bookId" to book.id.toString(),
                )
            )
                .enqueue { result ->
                    if (result.isSuccess) {
                        Log.d("MainActivity", "Book channel created: ${book.title}")
                    } else {
                        Log.e(
                            "MainActivity",
                            "Error creating book channel: ${result.errorOrNull()}"
                        )
                    }
                }
        }

        // Update query to include book channels
        val request =
            QueryChannelsRequest(
                filter =
                Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.`in`("members", listOf("tutorial-droid")),
                ),
                offset = 0,
                limit = 30,
                querySort = QuerySortByField.descByName("lastMessageAt")
            )
                .apply {
                    watch = true
                    state = true
                }

        // Add error logging
        client.queryChannels(request).enqueue { result ->
            if (result.isSuccess) {
                val channels: List<Channel> = result.getOrThrow()
                Log.d("MainActivity", "Channels retrieved: ${channels.size}")
            } else {
                Log.e("MainActivity", "Error querying channels: ${result.errorOrNull()}")
            }
        }

        ChannelsScreen(
            viewModelFactory = ChannelViewModelFactory(client),
            title = "Library Chat",
            isShowingHeader = true,
            onChannelClick = { channel ->
                selectedChannel = channel
            },
            onBackPressed = {}
        )
    }
}
