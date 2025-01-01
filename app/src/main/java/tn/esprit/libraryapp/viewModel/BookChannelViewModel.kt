package tn.esprit.libraryapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.models.ChatMessage
import tn.esprit.libraryapp.repository.BookRepository
import tn.esprit.libraryapp.services.ChatSocketService
import tn.esprit.libraryapp.services.TokenManagerProvider
import tn.esprit.libraryapp.services.VoiceCallService

class BookChannelViewModel : ViewModel() {
    private val repository = BookRepository()
    private val chatService = ChatSocketService()
    private val voiceCallService = VoiceCallService(chatService)
    private val _bookmarkedBooks = MutableStateFlow<List<Book>>(emptyList())
    val bookmarkedBooks: StateFlow<List<Book>> = _bookmarkedBooks

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private var currentBookId: String? = null

    private val _isInCall = MutableStateFlow(false)
    val isInCall: StateFlow<Boolean> = _isInCall

    private val _incomingCall = MutableStateFlow<ChatSocketService.IncomingCallData?>(null)
    val incomingCall: StateFlow<ChatSocketService.IncomingCallData?> = _incomingCall

    private val _permissionNeeded = MutableStateFlow(false)
    val permissionNeeded: StateFlow<Boolean> = _permissionNeeded

    init {
        fetchBookmarkedBooks()
        observeCallState()
        startMessageCollection()
        observePreviousMessages()
        observeIncomingCalls()
    }

    private fun observeCallState() {
        viewModelScope.launch {
            voiceCallService.callState.collect { state ->
                _isInCall.value = state != VoiceCallService.CallState.Idle
            }
        }
    }

    private fun startMessageCollection() {
        viewModelScope.launch {
            try {
                while (true) {
                    val message = chatService.receiveMessages()
                    _chatMessages.value = _chatMessages.value + message
                }
            } catch (e: Exception) {
                Log.e("ChatSocket", "Error collecting messages", e)
            }
        }
    }

    private fun observePreviousMessages() {
        viewModelScope.launch {
            chatService.previousMessages.collect { messages ->
                if (messages.isNotEmpty()) {
                    _chatMessages.value = messages
                }
            }
        }
    }

    private fun observeIncomingCalls() {
        viewModelScope.launch {
            chatService.incomingCall.collect { callData ->
                _incomingCall.value = callData
            }
        }
    }

    fun fetchBookmarkedBooks() {
        viewModelScope.launch {
            try {
                val userId = TokenManagerProvider.getInstance().userId.first()
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

    fun openBookChat(bookId: String) {
        currentBookId = bookId
        _chatMessages.value = emptyList()
        chatService.connect()
        viewModelScope.launch {
            val userId = TokenManagerProvider.getInstance().userId.first()
            userId?.let {
                chatService.joinRoom(bookId, it, "User_$it")
            }
        }
    }

    fun closeBookChat() {
        currentBookId?.let { bookId ->
            viewModelScope.launch {
                val userId = TokenManagerProvider.getInstance().userId.first()
                chatService.leaveRoom(bookId, "User_$userId")
                chatService.disconnect()
            }
        }
    }

    fun sendMessage(bookId: String, content: String) {
        viewModelScope.launch {
            val userId = TokenManagerProvider.getInstance().userId.first()
            userId?.let {
                chatService.sendMessage(bookId, it, "User_$it", content)
            }
        }
    }

    fun startVoiceCall(bookId: String) {
        viewModelScope.launch {
            val userId = TokenManagerProvider.getInstance().userId.first()
            userId?.let {
                try {
                    _isInCall.value = true
                    voiceCallService.startCall(bookId, it, "User_$it")
                } catch (e: SecurityException) {
                    _permissionNeeded.value = true
                    _isInCall.value = false
                }
            }
        }
    }

    fun onPermissionGranted(bookId: String) {
        _permissionNeeded.value = false
        startVoiceCall(bookId)
    }

    fun endVoiceCall() {
        voiceCallService.endCall()
    }

    fun acceptCall(roomId: String) {
        viewModelScope.launch {
            val userId = TokenManagerProvider.getInstance().userId.first()
            userId?.let {
                voiceCallService.joinCall(roomId, it, "User_$it")
                _incomingCall.value = null
            }
        }
    }

    fun rejectCall() {
        viewModelScope.launch {
            val userId = TokenManagerProvider.getInstance().userId.first()
            _incomingCall.value?.let { call ->
                chatService.declineCall(call.roomId, "User_$userId")
            }
            _incomingCall.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        closeBookChat()
        endVoiceCall()
    }

    fun getCurrentUserId(): String {
        return "current_user"
    }
}
