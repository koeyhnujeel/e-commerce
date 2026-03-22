package zoonza.commerce.review.application.dto

data class UpdateReviewCommand(
    val rating: Int,
    val content: String,
)
