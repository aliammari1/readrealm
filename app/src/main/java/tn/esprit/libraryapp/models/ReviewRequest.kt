package tn.esprit.libraryapp.models

data class AddReviewRequest(
    val userId: String,
    val book: Book,
    val comment: String
)

data class DeleteReviewRequest(
    val userId: String,
    val bookId: String,
    val reviewId: String
)

data class ReviewRequest(
    val userId: String,
    val rating: Int,
    val comment: String
)
