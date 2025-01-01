package tn.esprit.libraryapp.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import tn.esprit.libraryapp.viewModel.BookChannelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookChannelScreen(
    navController: NavHostController,
    viewModel: BookChannelViewModel = viewModel()
) {
    val bookmarkedBooks by viewModel.bookmarkedBooks.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchBookmarkedBooks()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Book Channels") },
                colors =
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bookmarkedBooks) { book ->
                BookChannelItem(
                    bookTitle = book.title,
                    bookCover = book.coverImage ?: "",
                    lastMessage = "Join the discussion!",
                    onChannelClick = {
                        book.id?.let { id -> navController.navigate("book_chat/$id") }
                    }
                )
            }
        }
    }
}

@Composable
fun BookChannelItem(
    bookTitle: String,
    bookCover: String,
    lastMessage: String,
    onChannelClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp), onClick = onChannelClick
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = bookCover,
                contentDescription = "Book cover",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(text = bookTitle, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BookChatScreen(bookId: String, viewModel: BookChannelViewModel = viewModel()) {
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.chatMessages.collectAsState()
    val incomingCall by viewModel.incomingCall.collectAsState()
    val permissionNeeded by viewModel.permissionNeeded.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.onPermissionGranted(bookId)
        }
    }

    LaunchedEffect(bookId) {
        viewModel.openBookChat(bookId)
    }

    DisposableEffect(bookId) {
        onDispose {
            viewModel.closeBookChat()
        }
    }

    // Show permission dialog if needed
    if (permissionNeeded) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Permission Required") },
            text = { Text("Microphone permission is needed for voice calls") },
            confirmButton = {
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) {
                    Text("Grant Permission")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Messages list with reverse layout
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top, // Stack from top
                state = rememberLazyListState() // Add list state for scrolling
            ) {
                items(messages.sortedByDescending { it.timestamp }) { message -> // Reverse sort order
                    ChatMessage(
                        message = message.content,
                        isOwnMessage = message.userId == viewModel.getCurrentUserId(),
                        senderName = message.userName
                    )
                }
            }

            // Bottom bar with message input and buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                shadowElevation = 4.dp // Add shadow to make it stand out
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp), // Increased padding
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Message input
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") }
                    )

                    // Voice call button
                    IconButton(
                        onClick = { viewModel.startVoiceCall(bookId) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Start voice call",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Send message button
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(bookId, messageText)
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send message",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        // Show call UI when in call
        if (viewModel.isInCall.collectAsState().value) {
            VoiceCallOverlay(
                onEndCall = { viewModel.endVoiceCall() }
            )
        }

        // Show incoming call dialog
        incomingCall?.let { call ->
            IncomingCallDialog(
                caller = call.caller,
                onAccept = { viewModel.acceptCall(call.roomId) },
                onReject = { viewModel.rejectCall() }
            )
        }
    }
}

@Composable
fun IncomingCallDialog(
    caller: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onReject,
        title = { Text("Incoming Call") },
        text = { Text("$caller is calling...") },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Accept")
            }
        },
        dismissButton = {
            Button(
                onClick = onReject,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Reject")
            }
        }
    )
}

@Composable
fun VoiceCallOverlay(onEndCall: () -> Unit) {
    // Update the existing VoiceCallOverlay to show connected users
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Voice Call in Progress",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            // End call button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "End call",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

@Composable
fun ChatMessage(
    message: String,
    isOwnMessage: Boolean,
    senderName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp), // Reduced vertical padding
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        Text(
            text = senderName,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isOwnMessage) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(max = 280.dp) // Limit maximum width of messages
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
