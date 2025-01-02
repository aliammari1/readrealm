package tn.esprit.libraryapp.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.video.android.compose.permission.LaunchCallPermissions
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.call.activecall.CallContent
import io.getstream.video.android.compose.ui.components.call.ringing.RingingCallContent
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.core.model.CallStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.repository.BookRepository
import tn.esprit.libraryapp.services.TokenManagerProvider

@Preview(showBackground = true)
@Composable
fun BookChatScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var userId by remember { mutableStateOf<String?>(null) }
    val client = remember {
        ChatClient.Builder("r2mnrp6eqtza", context)
            .withPlugins(
                StreamOfflinePluginFactory(appContext = context),
                StreamStatePluginFactory(config = StatePluginConfig(), appContext = context)
            )
            .logLevel(ChatLogLevel.ALL)
            .build()
    }

    // Add DisposableEffect to handle cleanup
    DisposableEffect(Unit) {
        onDispose {
            client.disconnect(true).enqueue { result ->
                if (result.isSuccess) {
                    Log.d("ChatClient", "User disconnected successfully")
                } else {
                    Log.e("ChatClient", "Error disconnecting user: ${result.errorOrNull()}")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        userId = TokenManagerProvider.getInstance().userId.first()
    }

    userId?.let { uid ->
        // Create user with admin role to bypass permissions
        val user = User(
            id = uid,
            name = "User",
            image = "https://bit.ly/2TIt8NR",
        )

        // Use token with admin rights
        val token = client.devToken(user.id)
        client.connectUser(user = user, token = token).execute()

        ChatTheme {
            when (client.clientState.initializationState.collectAsState().value) {
                InitializationState.COMPLETE -> ChatChannelScreen(client = client)
                InitializationState.INITIALIZING -> Text("Initializing...")
                InitializationState.NOT_INITIALIZED -> Text("Not initialized...")
                else -> Text("Unknown state")
            }
        }
    }
}

class CustomChannelListViewModel(private val chatClient: ChatClient = ChatClient.instance()) :
    ViewModel() {
    private val _bookmarkedBooks = MutableStateFlow<List<Book>>(emptyList())
    val bookmarkedBooks: StateFlow<List<Book>> = _bookmarkedBooks

    init {
        fetchBookmarkedBooks()
    }

    fun fetchBookmarkedBooks() {
        viewModelScope.launch {
            try {
                val userId = TokenManagerProvider.getInstance().userId.first()
                userId?.let {
                    _bookmarkedBooks.value = BookRepository().getBookmarks(it)
                }
            } catch (e: Exception) {
                Log.e("BookChannelViewModel", "Error fetching bookmarks", e)
                _bookmarkedBooks.value = emptyList()
            }
        }
    }

    fun queryAllChannels() {
        viewModelScope.launch {
            try {
                val request = QueryChannelsRequest(
                    filter = Filters.eq("type", "messaging"),
                    offset = 0,
                    limit = 100,
                    querySort = QuerySortByField.descByName("lastMessageAt")
                ).apply {
                    // Enable watching for real-time updates
                    watch = true
                    // Enable member presence
                    presence = true
                    // Include state in response
                    state = true
                }

                chatClient.queryChannels(request).enqueue { result ->
                    if (result.isSuccess) {
                        val channels = result.getOrThrow()
                        channels.forEach { channel ->
                            Log.d(
                                "ChatChannel", """
                                Channel Details:
                                ID: ${channel.id}
                                CID: ${channel.cid}
                                Name: ${channel.name}
                                Members: ${channel.members.map { it.user.id }}
                                Created At: ${channel.createdAt}
                                Last Message At: ${channel.lastMessageAt}
                                Member Count: ${channel.memberCount}
                                -----------------------------
                            """.trimIndent()
                            )
                        }
                    } else {
                        Log.e("ChatChannel", "Failed to query channels: ${result.errorOrNull()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatChannel", "Exception during channel query", e)
            }
        }
    }
}

@Composable
fun ChatChannelScreen(
    viewModel: CustomChannelListViewModel = viewModel(),
    client: ChatClient = ChatClient.instance()
) {
    var selectedChannel by remember { mutableStateOf<Channel?>(null) }
    val context = LocalContext.current
    var currentUserId by remember { mutableStateOf<String?>(null) }
    val bookmarkedBooks by viewModel.bookmarkedBooks.collectAsState()

    LaunchedEffect(Unit) {
        currentUserId = TokenManagerProvider.getInstance().userId.first()
    }

    currentUserId?.let { uid ->
        if (selectedChannel != null) {
            var showVideoCall by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            
            if (showVideoCall) {
                val videoClient = remember {
                    StreamVideoBuilder(
                        context = context.applicationContext,
                        apiKey = "r2mnrp6eqtza",
                        user = io.getstream.video.android.model.User(
                            id = uid,
                            name = "User",
                            image = "https://bit.ly/2TIt8NR"
                        ),
                        token = client.devToken(uid),
                        geo = io.getstream.video.android.core.GEO.GlobalEdgeNetwork
                    ).build()
                }

                val call = remember {
                    videoClient.call(type = "default", id = selectedChannel!!.id)
                }

                var hasPermissions by remember { mutableStateOf(false) }

                VideoTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LaunchCallPermissions(
                            call = call,
                            onAllPermissionsGranted = {
                                hasPermissions = true
                            }
                        )

                        if (hasPermissions) {
                            LaunchedEffect(call) {
                                try {
                                    call.join(
                                        create = true,
                                        ring = false,
                                        notify = false
                                    ).onSuccess {
                                        Log.d("VideoCall", "Successfully joined call")
                                    }.onError { error ->
                                        Log.e("VideoCall", "Failed to join call: $error")
                                        Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                                        showVideoCall = false
                                    }
                                } catch (e: Exception) {
                                    Log.e("VideoCall", "Exception in call: ${e.message}")
                                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                                    showVideoCall = false
                                }
                            }

                            CallContent(
                                modifier = Modifier.fillMaxSize(),
                                call = call,
                                onBackPressed = {
                                    scope.launch {
                                        call.leave()
                                    }
                                    showVideoCall = false
                                }
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    MessagesScreen(
                        viewModelFactory = MessagesViewModelFactory(
                            context = context,
                            channelId = selectedChannel!!.cid,
                            enforceUniqueReactions = true,
                            messageLimit = 30
                        ),
                        onBackPressed = { selectedChannel = null }
                    )

                    // Add video call button as a floating action button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        IconButton(
                            onClick = { showVideoCall = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoCall,
                                contentDescription = "Start Video Call"
                            )
                        }
                    }
                }
            }
            return@let
        }

        // Add this LaunchedEffect to query channels when screen loads
        LaunchedEffect(Unit) {
            viewModel.queryAllChannels()
        }

        // Create/Update channels for bookmarked books
        LaunchedEffect(bookmarkedBooks) {
            bookmarkedBooks.forEach { book ->
                val channelId = "book-${book.id}"
                val channelClient = client.channel("messaging", channelId)

                // First, try to watch the channel to check if it exists
                channelClient.watch().enqueue { watchResult ->
                    when {
                        watchResult.isSuccess -> {
                            // Channel exists, check if user is a member
                            val channel = watchResult.getOrThrow()
                            Log.d(
                                "ChatChannel",
                                "Channel exists: ${channel.id}, Members: ${channel.members.map { it.user.id }}"
                            )

                            if (!channel.members.map { it.user.id }.contains(uid)) {
                                // User is not a member, add them
                                channelClient.addMembers(listOf(uid)).enqueue { addResult ->
                                    if (addResult.isSuccess) {
                                        Log.d(
                                            "ChatChannel",
                                            "Added user $uid to existing channel ${channel.id}"
                                        )
                                        // Update channel data
                                        channelClient.update(
                                            message = Message(text = "User joined the discussion"),
                                            extraData = mapOf(
                                                "name" to book.title,
                                                "image" to (book.coverImage ?: ""),
                                                "bookId" to book.id.toString()
                                            )
                                        ).enqueue()
                                    } else {
                                        Log.e(
                                            "ChatChannel",
                                            "Failed to add member: ${addResult.errorOrNull()}"
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            // Channel doesn't exist, create it
                            channelClient.create(
                                memberIds = listOf(uid),
                                extraData = mapOf(
                                    "name" to book.title,
                                    "image" to (book.coverImage ?: ""),
                                    "bookId" to book.id.toString()
                                )
                            ).enqueue { createResult ->
                                if (createResult.isSuccess) {
                                    Log.d(
                                        "ChatChannel",
                                        "Created new channel: $channelId with member $uid"
                                    )
                                    // Verify channel creation by watching it
                                    channelClient.watch().enqueue()
                                } else {
                                    Log.e(
                                        "ChatChannel",
                                        "Failed to create channel: ${createResult.errorOrNull()}"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        ChannelsScreen(
            viewModelFactory = ChannelViewModelFactory(client),
            title = "Library Chat",
            isShowingHeader = true,
            onChannelClick = { selectedChannel = it },
            onBackPressed = {}
        )
    }
}
