package zoonza.commerce.review.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResponse
import zoonza.commerce.member.MemberApi
import zoonza.commerce.order.OrderApi
import zoonza.commerce.review.ReviewErrorCode
import zoonza.commerce.review.application.dto.CreateReviewCommand
import zoonza.commerce.review.application.dto.ReviewDetail
import zoonza.commerce.review.application.dto.ReviewSummary
import zoonza.commerce.review.application.dto.UpdateReviewCommand
import zoonza.commerce.review.application.port.`in`.ReviewService
import zoonza.commerce.review.application.port.out.ReviewRepository
import zoonza.commerce.review.domain.Review
import zoonza.commerce.shared.BusinessException
import java.time.LocalDateTime

@Service
class DefaultReviewService(
    private val reviewRepository: ReviewRepository,
    private val catalogApi: CatalogApi,
    private val memberApi: MemberApi,
    private val orderApi: OrderApi,
) : ReviewService {
    @Transactional
    override fun create(
        memberId: Long,
        productId: Long,
        command: CreateReviewCommand,
    ): Long {
        validateProductExists(productId)

        val existingReview = reviewRepository.findByMemberIdAndProductId(memberId, productId)

        if (existingReview != null) {
            if (existingReview.deletedAt == null) {
                throw BusinessException(ReviewErrorCode.REVIEW_ALREADY_EXISTS)
            }

            existingReview.restore(
                rating = command.rating,
                content = command.content,
                restoredAt = LocalDateTime.now(),
            )

            return reviewRepository.save(existingReview).id
        }

        val reviewablePurchase = orderApi.findReviewablePurchase(memberId, productId).firstOrNull()
            ?: throw BusinessException(ReviewErrorCode.REVIEW_PURCHASE_REQUIRED)

        val review = Review.create(
            memberId = memberId,
            productId = productId,
            orderItemId = reviewablePurchase.orderItemId,
            optionColor = reviewablePurchase.option.color,
            optionSize = reviewablePurchase.option.size,
            rating = command.rating,
            content = command.content,
            createdAt = LocalDateTime.now(),
        )

        return reviewRepository.save(review).id
    }

    @Transactional(readOnly = true)
    override fun getProductReviews(
        productId: Long,
        page: Int,
        size: Int,
    ): PageResponse<ReviewSummary> {
        validateProductExists(productId)

        val reviewPage = reviewRepository.findByProductId(
            productId = productId,
            pageQuery = PageQuery(
                page = page,
                size = size,
            ),
        )
        val memberProfiles = memberApi.findProfilesByIds(reviewPage.items.map(Review::memberId).toSet())

        return PageResponse(
            items = reviewPage.items.map { review ->
                    toSummary(
                        review = review,
                        authorNickname = memberProfiles.getValue(review.memberId).nickname,
                    )
                },
            page = reviewPage.page,
            size = reviewPage.size,
            totalElements = reviewPage.totalElements,
            totalPages = reviewPage.totalPages,
        )
    }

    @Transactional(readOnly = true)
    override fun getMyReview(
        memberId: Long,
        productId: Long,
    ): ReviewDetail {
        validateProductExists(productId)

        val review = reviewRepository.findActiveByMemberIdAndProductId(memberId, productId)
            ?: throw BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND)

        val memberProfile = memberApi.findProfileById(review.memberId)

        return toDetail(review, memberProfile.nickname)
    }

    @Transactional
    override fun update(
        memberId: Long,
        productId: Long,
        command: UpdateReviewCommand,
    ) {
        validateProductExists(productId)

        val review = reviewRepository.findActiveByMemberIdAndProductId(memberId, productId)
            ?: throw BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND)

        review.update(
            rating = command.rating,
            content = command.content,
            updatedAt = LocalDateTime.now(),
        )

        reviewRepository.save(review)
    }

    @Transactional
    override fun delete(
        memberId: Long,
        productId: Long,
    ) {
        validateProductExists(productId)

        val review = reviewRepository.findActiveByMemberIdAndProductId(memberId, productId)
            ?: throw BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND)

        review.delete(LocalDateTime.now())

        reviewRepository.save(review)
    }

    private fun validateProductExists(productId: Long) {
        catalogApi.assertProductExists(productId)
    }

    private fun toSummary(
        review: Review,
        authorNickname: String,
    ): ReviewSummary {
        return ReviewSummary(
            id = review.id,
            authorNickname = authorNickname,
            optionColor = review.optionColor,
            optionSize = review.optionSize,
            rating = review.rating,
            content = review.content,
            createdAt = review.createdAt,
            updatedAt = review.updatedAt,
        )
    }

    private fun toDetail(
        review: Review,
        authorNickname: String,
    ): ReviewDetail {
        return ReviewDetail(
            id = review.id,
            productId = review.productId,
            authorNickname = authorNickname,
            optionColor = review.optionColor,
            optionSize = review.optionSize,
            rating = review.rating,
            content = review.content,
            createdAt = review.createdAt,
            updatedAt = review.updatedAt,
        )
    }
}
