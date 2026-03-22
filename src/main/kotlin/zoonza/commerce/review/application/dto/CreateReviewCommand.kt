package zoonza.commerce.review.application.dto

data class CreateReviewCommand(
    val rating: Int,
    val content: String,
)
