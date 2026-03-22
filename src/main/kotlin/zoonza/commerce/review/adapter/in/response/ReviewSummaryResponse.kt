package zoonza.commerce.review.adapter.`in`.response

import java.time.LocalDateTime

data class ReviewSummaryResponse(
    val reviewId: Long,
    val authorNickname: String,
    val optionColor: String,
    val optionSize: String,
    val rating: Int,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
