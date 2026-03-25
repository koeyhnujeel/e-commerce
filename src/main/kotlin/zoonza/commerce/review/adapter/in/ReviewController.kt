package zoonza.commerce.review.adapter.`in`

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.support.web.ApiResponse
import zoonza.commerce.support.pagination.PageResponse
import zoonza.commerce.review.adapter.`in`.request.CreateReviewRequest
import zoonza.commerce.review.adapter.`in`.request.UpdateReviewRequest
import zoonza.commerce.review.adapter.`in`.response.CreateReviewResponse
import zoonza.commerce.review.adapter.`in`.response.MyReviewResponse
import zoonza.commerce.review.adapter.`in`.response.ReviewSummaryResponse
import zoonza.commerce.review.application.dto.CreateReviewCommand
import zoonza.commerce.review.application.dto.ReviewDetail
import zoonza.commerce.review.application.dto.ReviewSummary
import zoonza.commerce.review.application.dto.UpdateReviewCommand
import zoonza.commerce.review.application.port.`in`.ReviewService
import zoonza.commerce.security.CurrentMember

@Validated
@RestController
@RequestMapping("/api/products/{productId}/reviews")
class ReviewController(
    private val reviewService: ReviewService,
) {
    @PostMapping
    fun createReview(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable productId: Long,
        @Valid @RequestBody request: CreateReviewRequest,
    ): ApiResponse<CreateReviewResponse> {
        val reviewId = reviewService.create(
            memberId = currentMember.memberId,
            productId = productId,
            command = CreateReviewCommand(
                rating = request.rating,
                content = request.content,
            ),
        )

        return ApiResponse.success(CreateReviewResponse(reviewId))
    }

    @GetMapping
    fun getProductReviews(
        @PathVariable productId: Long,
        @RequestParam(defaultValue = "1")
        @Positive(message = "페이지 번호는 1 이상이어야 합니다.")
        page: Int,
        @RequestParam(defaultValue = "20")
        @Positive(message = "페이지 크기는 1 이상이어야 합니다.")
        size: Int,
    ): ApiResponse<PageResponse<ReviewSummaryResponse>> {
        val reviews = reviewService.getProductReviews(productId, page - 1, size)

        return ApiResponse.success(
            PageResponse(
                items = reviews.items.map(::toSummaryResponse),
                page = reviews.page + 1,
                size = reviews.size,
                totalElements = reviews.totalElements,
                totalPages = reviews.totalPages,
            ),
        )
    }

    @GetMapping("/me")
    fun getMyReview(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable productId: Long,
    ): ApiResponse<MyReviewResponse> {
        val review = reviewService.getMyReview(currentMember.memberId, productId)

        return ApiResponse.success(toMyReviewResponse(review))
    }

    @PutMapping("/me")
    fun updateReview(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable productId: Long,
        @Valid @RequestBody request: UpdateReviewRequest,
    ): ApiResponse<Nothing> {
        reviewService.update(
            memberId = currentMember.memberId,
            productId = productId,
            command = UpdateReviewCommand(
                rating = request.rating,
                content = request.content,
            ),
        )

        return ApiResponse.success()
    }

    @DeleteMapping("/me")
    fun deleteReview(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable productId: Long,
    ): ApiResponse<Nothing> {
        reviewService.delete(currentMember.memberId, productId)

        return ApiResponse.success()
    }

    private fun toSummaryResponse(review: ReviewSummary): ReviewSummaryResponse {
        return ReviewSummaryResponse(
            reviewId = review.id,
            authorNickname = review.authorNickname,
            optionColor = review.optionColor,
            optionSize = review.optionSize,
            rating = review.rating,
            content = review.content,
            createdAt = review.createdAt,
            updatedAt = review.updatedAt,
        )
    }

    private fun toMyReviewResponse(review: ReviewDetail): MyReviewResponse {
        return MyReviewResponse(
            reviewId = review.id,
            productId = review.productId,
            authorNickname = review.authorNickname,
            optionColor = review.optionColor,
            optionSize = review.optionSize,
            rating = review.rating,
            content = review.content,
            createdAt = review.createdAt,
            updatedAt = review.updatedAt,
        )
    }
}
