package tn.esprit.libraryapp.models

data class ChatMessage(
    val id: String,
    val bookId: String,
    val userId: String,
    val userName: String,
    val content: String,
    val timestamp: Long,
)
