package zoonza.commerce.review.adapter.`in`.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CreateReviewRequest(
    @field:Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @field:Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    val rating: Int,

    @field:NotBlank(message = "리뷰 내용은 필수입니다.")
    val content: String,
)
