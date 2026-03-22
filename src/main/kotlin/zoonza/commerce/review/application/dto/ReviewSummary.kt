package zoonza.commerce.review.application.dto

import java.time.LocalDateTime

data class ReviewSummary(
    val id: Long,
    val authorNickname: String,
    val optionColor: String,
    val optionSize: String,
    val rating: Int,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
