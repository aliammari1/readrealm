package tn.esprit.libraryapp.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import io.getstream.video.android.core.GEO
import io.getstream.video.android.core.StreamVideoBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.repository.BookRepository
import tn.esprit.libraryapp.services.TokenManagerProvider
import tn.esprit.libraryapp.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════════
// ✨ WIZARD'S DIALOGUE HALL ✨
// A mystical chat interface for book discussions
// ═══════════════════════════════════════════════════════════════════

@Preview(showBackground = true)
@Composable
fun BookChatScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var userId by remember { mutableStateOf<String?>(null) }
    val client = remember {
        ChatClient.Builder("r2mnrp6eqtza", context)
            .withPlugins(
                StreamOfflinePluginFactory(appContext = context),
                StreamStatePluginFactory(config = StatePluginConfig(), appContext = context),
            )
            .logLevel(ChatLogLevel.ALL)
            .build()
    }

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
        val user = User(
            id = uid,
            name = "User",
            image = "https://bit.ly/2TIt8NR",
        )

        val token = client.devToken(user.id)
        client.connectUser(user = user, token = token).execute()

        Box(modifier = modifier.fillMaxSize()) {
            // Mystical chat background
            MysticalChatBackground()
            
            // Floating magical particles
            FloatingChatParticles()
            
            ChatTheme {
                when (client.clientState.initializationState.collectAsState().value) {
                    InitializationState.COMPLETE -> {
                        EnchantedChatChannelScreen(client = client)
                    }
                    InitializationState.INITIALIZING -> {
                        MysticalLoadingState(message = "Connecting to the arcane network...")
                    }
                    InitializationState.NOT_INITIALIZED -> {
                        MysticalLoadingState(message = "Preparing magical channels...")
                    }
                    else -> {
                        MysticalLoadingState(message = "Unknown mystical state...")
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// MYSTICAL CHAT BACKGROUND
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun MysticalChatBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepLibraryBrown,
                            MysticPurple.copy(alpha = 0.3f),
                            InkBlue.copy(alpha = 0.4f),
                            DeepLibraryBrown
                        )
                    )
                )
        )
        
        // Mystical pattern overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw subtle magical circles
            val centerX = size.width / 2
            val centerY = size.height / 3
            
            for (i in 1..4) {
                drawCircle(
                    color = GildedGold.copy(alpha = 0.05f),
                    radius = 80f * i,
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// FLOATING CHAT PARTICLES
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun FloatingChatParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "chatParticles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "chatParticleTime"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val particleCount = 20
        for (i in 0 until particleCount) {
            val phase = i.toFloat() / particleCount
            val x = size.width * ((sin(time * 2 * Math.PI + phase * Math.PI * 4) + 1) / 2).toFloat()
            val y = size.height * ((phase + time * 0.5f) % 1f)
            val alpha = (sin(time * 3 * Math.PI + i) + 1) / 4
            
            drawCircle(
                color = CandlelightGlow.copy(alpha = alpha.toFloat()),
                radius = 2f + (i % 2),
                center = Offset(x, y)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// MYSTICAL LOADING STATE
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun MysticalLoadingState(message: String) {
    val rotateAnimation = rememberInfiniteTransition(label = "loadRotate")
    val rotation by rotateAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulseAnimation by rotateAnimation.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rotating magical runes
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer { rotationZ = rotation },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val runeCount = 6
                    for (i in 0 until runeCount) {
                        val angle = (360f / runeCount) * i
                        val radian = Math.toRadians(angle.toDouble())
                        val x = center.x + (size.minDimension / 2 - 15) * cos(radian).toFloat()
                        val y = center.y + (size.minDimension / 2 - 15) * sin(radian).toFloat()
                        drawCircle(
                            color = GildedGold,
                            radius = 8.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
                
                // Inner orb
                Box(
                    modifier = Modifier
                        .size((60 * pulseAnimation).dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MysticPurple,
                                    MysticPurple.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = message,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = CandlelightGlow
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// ENCHANTED CHAT CHANNEL SCREEN
// ═══════════════════════════════════════════════════════════════════

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
                    querySort = QuerySortByField.descByName("lastMessageAt"),
                ).apply {
                    watch = true
                    presence = true
                    state = true
                }

                chatClient.queryChannels(request).enqueue { result ->
                    if (result.isSuccess) {
                        val channels = result.getOrThrow()
                        channels.forEach { channel ->
                            Log.d(
                                "ChatChannel",
                                """
                                Channel Details:
                                ID: ${channel.id}
                                CID: ${channel.cid}
                                Name: ${channel.name}
                                Members: ${channel.members.map { it.user.id }}
                                Created At: ${channel.createdAt}
                                Last Message At: ${channel.lastMessageAt}
                                Member Count: ${channel.memberCount}
                                -----------------------------
                                """.trimIndent(),
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
fun EnchantedChatChannelScreen(
    viewModel: CustomChannelListViewModel = viewModel(),
    client: ChatClient = ChatClient.instance(),
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
                            image = "https://bit.ly/2TIt8NR",
                        ),
                        token = client.devToken(uid),
                        geo = GEO.GlobalEdgeNetwork,
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
                            },
                        )

                        if (hasPermissions) {
                            LaunchedEffect(call) {
                                try {
                                    call.join(
                                        create = true,
                                        ring = false,
                                        notify = false,
                                    ).onSuccess {
                                        Log.d("VideoCall", "Successfully joined call")
                                    }.onError { error ->
                                        Log.e("VideoCall", "Failed to join call: $error")
                                        Toast.makeText(context, error.message, Toast.LENGTH_LONG)
                                            .show()
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
                                },
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
                            messageLimit = 30,
                        ),
                        onBackPressed = { selectedChannel = null },
                    )

                    // Mystical video call button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        MysticalVideoCallButton(
                            onClick = { showVideoCall = true }
                        )
                    }
                }
            }
            return@let
        }

        LaunchedEffect(Unit) {
            viewModel.queryAllChannels()
        }

        LaunchedEffect(bookmarkedBooks) {
            bookmarkedBooks.forEach { book ->
                val channelId = "book-${book.id}"
                val channelClient = client.channel("messaging", channelId)

                channelClient.watch().enqueue { watchResult ->
                    when {
                        watchResult.isSuccess -> {
                            val channel = watchResult.getOrThrow()
                            Log.d(
                                "ChatChannel",
                                "Channel exists: ${channel.id}, Members: ${channel.members.map { it.user.id }}",
                            )

                            if (!channel.members.map { it.user.id }.contains(uid)) {
                                channelClient.addMembers(listOf(uid)).enqueue { addResult ->
                                    if (addResult.isSuccess) {
                                        Log.d(
                                            "ChatChannel",
                                            "Added user $uid to existing channel ${channel.id}",
                                        )
                                        channelClient.update(
                                            message = Message(text = "User joined the discussion"),
                                            extraData = mapOf(
                                                "name" to book.title,
                                                "image" to (book.coverImage ?: ""),
                                                "bookId" to book.id.toString(),
                                            ),
                                        ).enqueue()
                                    } else {
                                        Log.e(
                                            "ChatChannel",
                                            "Failed to add member: ${addResult.errorOrNull()}",
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            channelClient.create(
                                memberIds = listOf(uid),
                                extraData = mapOf(
                                    "name" to book.title,
                                    "image" to (book.coverImage ?: ""),
                                    "bookId" to book.id.toString(),
                                ),
                            ).enqueue { createResult ->
                                if (createResult.isSuccess) {
                                    Log.d(
                                        "ChatChannel",
                                        "Created new channel: $channelId with member $uid",
                                    )
                                    channelClient.watch().enqueue()
                                } else {
                                    Log.e(
                                        "ChatChannel",
                                        "Failed to create channel: ${createResult.errorOrNull()}",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Mystical header
            EnchantedChatHeader()
            
            // Channels list
            ChannelsScreen(
                viewModelFactory = ChannelViewModelFactory(client),
                title = "",
                isShowingHeader = false,
                onChannelClick = { selectedChannel = it },
                onBackPressed = {},
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// ENCHANTED CHAT HEADER
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun EnchantedChatHeader() {
    val glowAnimation = rememberInfiniteTransition(label = "headerGlow")
    val glowAlpha by glowAnimation.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        DeepLibraryBrown,
                        MysticPurple.copy(alpha = 0.5f),
                        DeepLibraryBrown
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        GildedGold.copy(alpha = 0.3f),
                        GildedGold.copy(alpha = glowAlpha),
                        GildedGold.copy(alpha = 0.3f)
                    )
                ),
                shape = CircleShape
            )
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💬",
                fontSize = 28.sp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Wizard's Dialogue Hall",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GildedGold
                )
                Text(
                    text = "Discuss tomes with fellow readers",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = CandlelightGlow.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// MYSTICAL VIDEO CALL BUTTON
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun MysticalVideoCallButton(onClick: () -> Unit) {
    val pulseAnimation = rememberInfiniteTransition(label = "videoPulse")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    Box(
        modifier = Modifier
            .size(50.dp)
            .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MysticPurple,
                        MysticPurple.copy(alpha = 0.5f)
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = GildedGold,
                shape = CircleShape
            )
            .clip(CircleShape)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.VideoCall,
                contentDescription = "Start Video Call",
                tint = GildedGold,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// Keep original function name for compatibility
@Composable
fun ChatChannelScreen(
    viewModel: CustomChannelListViewModel = viewModel(),
    client: ChatClient = ChatClient.instance(),
) {
    EnchantedChatChannelScreen(viewModel = viewModel, client = client)
}
