package tn.esprit.libraryapp.services

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import tn.esprit.libraryapp.models.ChatMessage

class ChatWebSocketService {
    private var socket: Socket? = null
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    init {
        try {
            socket = IO.socket("http://192.168.17.105:3000/")
            setupSocketListeners()
            socket?.connect()
        } catch (e: Exception) {
            Log.e("WebSocket", "Error initializing socket", e)
        }
    }

    private fun setupSocketListeners() {
        socket?.let { socket ->
            socket.on("previousMessages") { args ->
                args[0]?.let { data ->
                    val messages = parseMessages(data.toString())
                    _messages.value = messages
                }
            }

            socket.on("newMessage") { args ->
                args[0]?.let { data ->
                    val message = parseMessage(data.toString())
                    _messages.value = _messages.value + message
                }
            }
        }
    }

    fun joinRoom(bookId: Int, userId: String, username: String) {
        val data =
                JSONObject().apply {
                    put("bookId", bookId)
                    put("userId", userId)
                    put("username", username)
                }
        socket?.emit("joinRoom", data)
    }

    fun leaveRoom(bookId: Int, username: String) {
        val data =
                JSONObject().apply {
                    put("bookId", bookId)
                    put("username", username)
                }
        socket?.emit("leaveRoom", data)
    }

    fun sendMessage(bookId: Int, userId: String, username: String, content: String) {
        val data =
                JSONObject().apply {
                    put("bookId", bookId)
                    put("userId", userId)
                    put("username", username)
                    put("content", content)
                }
        socket?.emit("chatMessage", data)
    }

    private fun parseMessage(jsonString: String): ChatMessage {
        val json = JSONObject(jsonString)
        return ChatMessage(
                id = json.getString("id"),
                bookId = json.getInt("bookId").toString(),
                userId = json.getString("userId"),
                userName = json.getString("username"),
                content = json.getString("content"),
                timestamp = json.getLong("timestamp")
        )
    }

    private fun parseMessages(jsonString: String): List<ChatMessage> {
        return try {
            // Parse JSON array of messages
            // Implementation depends on your backend response format
            emptyList() // Temporary return until format is known
        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing messages", e)
            emptyList()
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
}
