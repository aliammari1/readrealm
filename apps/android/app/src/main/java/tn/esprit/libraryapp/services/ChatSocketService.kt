package tn.esprit.libraryapp.services

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import tn.esprit.libraryapp.models.ChatMessage

class ChatSocketService {
    private var socket: Socket? = null
    private val messageChannel = Channel<ChatMessage>()
    private val scope = CoroutineScope(Dispatchers.IO)
    val connectionState = MutableStateFlow(false)
    private val _previousMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val previousMessages: StateFlow<List<ChatMessage>> = _previousMessages
    private val _incomingCall = MutableStateFlow<IncomingCallData?>(null)
    val incomingCall: StateFlow<IncomingCallData?> = _incomingCall
    private val _voiceData = MutableSharedFlow<ByteArray>()
    val voiceData: SharedFlow<ByteArray> = _voiceData
    private val _participants = MutableStateFlow<Set<String>>(emptySet())
    val participants: StateFlow<Set<String>> = _participants

    init {
        try {
            val options =
                    IO.Options.builder()
                            .setTransports(arrayOf("websocket"))
                            .setForceNew(true)
                            .build()
            socket = IO.socket("https://libraryapp-nest-back.vercel.app/", options)
            setupSocketListeners()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    private fun setupSocketListeners() {
        socket?.let { socket ->
            socket.on(Socket.EVENT_CONNECT) { connectionState.value = true }

            socket.on(Socket.EVENT_DISCONNECT) { connectionState.value = false }

            socket.on("previousMessages") { args ->
                args.firstOrNull()?.let { data ->
                    try {
                        val messagesArray = data as JSONArray
                        val messages =
                                (0 until messagesArray.length()).map { index ->
                                    val jsonMessage = messagesArray.getJSONObject(index)
                                    ChatMessage(
                                            id = jsonMessage.optString("_id"),
                                            bookId = jsonMessage.getString("bookId"),
                                            userId = jsonMessage.getString("userId"),
                                            userName = jsonMessage.getString("username"),
                                            content = jsonMessage.getString("content"),
                                            timestamp =
                                                    try {
                                                        val dateStr =
                                                                jsonMessage.getString("createdAt")
                                                        java.text.SimpleDateFormat(
                                                                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                                                        java.util.Locale.US,
                                                                )
                                                                .parse(dateStr)
                                                                ?.time
                                                                ?: System.currentTimeMillis()
                                                    } catch (e: Exception) {
                                                        System.currentTimeMillis()
                                                    },
                                    )
                                }
                        _previousMessages.value = messages
                    } catch (e: Exception) {
                        Log.e("ChatSocketService", "Error parsing previous messages", e)
                    }
                }
            }

            socket.on("newMessage") { args ->
                args.firstOrNull()?.let { data ->
                    val jsonData = data as JSONObject
                    val message =
                            ChatMessage(
                                    id = jsonData.optString("_id"),
                                    bookId = jsonData.getString("bookId"),
                                    userId = jsonData.getString("userId"),
                                    userName = jsonData.getString("username"),
                                    content = jsonData.getString("content"),
                                    timestamp = jsonData.optLong("createdAt")
                                                    ?: System.currentTimeMillis(),
                            )
                    messageChannel.trySend(message)
                }
            }

            socket.on("incomingCall") { args ->
                args.firstOrNull()?.let { data ->
                    try {
                        val jsonData = data as JSONObject
                        val caller = jsonData.getString("caller")
                        val roomId = jsonData.getString("roomId")
                        val bookId = jsonData.getString("bookId")
                        _incomingCall.value = IncomingCallData(caller, roomId, bookId)
                        Log.d("ChatSocket", "Incoming call received from $caller for room $roomId")
                    } catch (e: Exception) {
                        Log.e("ChatSocket", "Error parsing incoming call data", e)
                    }
                }
            }

            socket.on("callDeclined") { args ->
                args.firstOrNull()?.let { data ->
                    val jsonData = data as JSONObject
                    val username = jsonData.getString("username")
                    Log.d("ChatSocket", "Call declined by $username")
                }
            }

            socket.on("voiceData") { args ->
                args.firstOrNull()?.let { data ->
                    try {
                        val jsonData = data as JSONObject
                        val base64Audio = jsonData.getString("audioChunk")
                        // Convert Base64 string back to ByteArray
                        val audioChunk =
                                android.util.Base64.decode(base64Audio, android.util.Base64.NO_WRAP)
                        scope.launch { _voiceData.emit(audioChunk) }
                    } catch (e: Exception) {
                        Log.e("ChatSocket", "Error handling voice data", e)
                    }
                }
            }

            socket.on("userJoinedCall") { args ->
                args.firstOrNull()?.let { data ->
                    try {
                        val jsonData = data as JSONObject
                        val userId = jsonData.getString("userId")
                        _participants.value = _participants.value + userId
                        Log.d("ChatSocket", "User joined call: $userId")
                    } catch (e: Exception) {
                        Log.e("ChatSocket", "Error handling user joined", e)
                    }
                }
            }

            socket.on("callEnded") {
                _participants.value = emptySet()
                _incomingCall.value = null
            }
        }
    }

    fun connect() {
        if (!connectionState.value) {
            socket?.connect()
        }
    }

    fun disconnect() {
        if (connectionState.value) {
            socket?.disconnect()
            _previousMessages.value = emptyList()
        }
    }

    fun joinRoom(bookId: String, userId: String, username: String) {
        val data =
                JSONObject().apply {
                    put("bookId", bookId)
                    put("userId", userId)
                    put("username", username)
                }
        socket?.emit("joinRoom", data)
    }

    fun leaveRoom(bookId: String, username: String) {
        val data =
                JSONObject().apply {
                    put("bookId", bookId)
                    put("username", username)
                }
        socket?.emit("leaveRoom", data)
    }

    fun sendMessage(bookId: String, userId: String, username: String, content: String) {
        val data =
                JSONObject().apply {
                    put("bookId", bookId)
                    put("userId", userId)
                    put("username", username)
                    put("content", content)
                }
        socket?.emit("chatMessage", data)
    }

    fun startVoiceCall(bookId: String, userId: String, username: String) {
        val data =
                JSONObject().apply {
                    put("bookId", bookId)
                    put("userId", userId)
                    put("username", username)
                }
        socket?.emit("voiceCallStart", data)
    }

    fun joinVoiceCall(roomId: String, userId: String, username: String) {
        val data =
                JSONObject().apply {
                    put("roomId", roomId)
                    put("userId", userId)
                    put("username", username)
                }
        socket?.emit("voiceCallJoin", data)
    }

    fun sendVoiceData(roomId: String, userId: String, audioChunk: ByteArray) {
        try {
            val data =
                    JSONObject().apply {
                        put("roomId", roomId)
                        put("userId", userId)
                        // Convert ByteArray to Base64 string for transmission
                        put(
                                "audioChunk",
                                android.util.Base64.encodeToString(
                                        audioChunk,
                                        android.util.Base64.NO_WRAP,
                                ),
                        )
                    }
            socket?.emit("voiceStream", data)
        } catch (e: Exception) {
            Log.e("ChatSocket", "Error sending voice data", e)
        }
    }

    fun endVoiceCall(roomId: String) {
        val data = JSONObject().apply { put("roomId", roomId) }
        socket?.emit("endCall", data)
    }

    fun declineCall(roomId: String, username: String) {
        val data =
                JSONObject().apply {
                    put("roomId", roomId)
                    put("username", username)
                }
        socket?.emit("declineCall", data)
    }

    suspend fun receiveMessages(): ChatMessage {
        return messageChannel.receive()
    }

    fun cleanup() {
        scope.cancel()
    }

    data class IncomingCallData(val caller: String, val roomId: String, val bookId: String)
}
