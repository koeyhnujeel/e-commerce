package zoonza.commerce.review.application.port.`in`

import zoonza.commerce.common.PageResponse
import zoonza.commerce.review.application.dto.CreateReviewCommand
import zoonza.commerce.review.application.dto.ReviewDetail
import zoonza.commerce.review.application.dto.ReviewSummary
import zoonza.commerce.review.application.dto.UpdateReviewCommand

interface ReviewService {
    fun create(
        memberId: Long,
        productId: Long,
        command: CreateReviewCommand,
    ): Long

    fun getProductReviews(
        productId: Long,
        page: Int,
        size: Int,
    ): PageResponse<ReviewSummary>

    fun getMyReview(
        memberId: Long,
        productId: Long,
    ): ReviewDetail

    fun update(
        memberId: Long,
        productId: Long,
        command: UpdateReviewCommand,
    )

    fun delete(
        memberId: Long,
        productId: Long,
    )
}
