package zoonza.commerce.review.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.catalog.ProductOptionSnapshot
import zoonza.commerce.member.MemberApi
import zoonza.commerce.member.MemberErrorCode
import zoonza.commerce.member.MemberProfile
import zoonza.commerce.order.OrderApi
import zoonza.commerce.order.ReviewablePurchase
import zoonza.commerce.review.ReviewErrorCode
import zoonza.commerce.review.application.dto.CreateReviewCommand
import zoonza.commerce.review.application.port.out.ReviewRepository
import zoonza.commerce.review.domain.Review
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResponse
import zoonza.commerce.support.pagination.PageResult
import java.time.LocalDateTime

class DefaultReviewServiceTest {
    private val reviewRepository = mockk<ReviewRepository>()
    private val catalogApi = mockk<CatalogApi>()
    private val memberApi = mockk<MemberApi>()
    private val orderApi = mockk<OrderApi>()
    private val reviewService =
        DefaultReviewService(
            reviewRepository = reviewRepository,
            catalogApi = catalogApi,
            memberApi = memberApi,
            orderApi = orderApi,
        )

    @Test
    fun `배송 완료 주문상품이 있으면 리뷰를 작성할 수 있다`() {
        val savedReview = slot<Review>()
        val persistedReview = mockk<Review>()

        every { catalogApi.assertProductExists(10L) } returns Unit
        every { reviewRepository.findByMemberIdAndProductId(1L, 10L) } returns null
        every { orderApi.findReviewablePurchase(1L, 10L) } returns
            listOf(ReviewablePurchase(200L, ProductOptionSnapshot("BLACK", "M")))
        every { reviewRepository.save(capture(savedReview)) } returns persistedReview
        every { persistedReview.id } returns 1L

        val reviewId =
            reviewService.create(
                memberId = 1L,
                productId = 10L,
                command = CreateReviewCommand(rating = 5, content = "만족합니다."),
            )

        reviewId shouldBe 1L
        savedReview.captured.memberId shouldBe 1L
        savedReview.captured.productId shouldBe 10L
        savedReview.captured.orderItemId shouldBe 200L
        savedReview.captured.optionColor shouldBe "BLACK"
        savedReview.captured.optionSize shouldBe "M"
        verify(exactly = 0) { memberApi.findProfileById(any()) }
    }

    @Test
    fun `상품 리뷰 목록은 애플리케이션 페이징 타입으로 조회한다`() {
        val pageQuery = slot<PageQuery>()

        every { catalogApi.assertProductExists(10L) } returns Unit
        every {
            reviewRepository.findByProductId(
                productId = 10L,
                pageQuery = capture(pageQuery),
            )
        } returns PageResult(
            items = listOf(review()),
            page = 0,
            size = 20,
            totalElements = 1,
            totalPages = 1,
        )
        every { memberApi.findProfilesByIds(setOf(1L)) } returns mapOf(1L to MemberProfile(1L, "latest-reviewer"))

        val result: PageResponse<*> = reviewService.getProductReviews(productId = 10L, page = 0, size = 20)

        pageQuery.captured shouldBe PageQuery(page = 0, size = 20)
        result.items.size shouldBe 1
        result.page shouldBe 0
        result.size shouldBe 20
        result.totalElements shouldBe 1
        result.totalPages shouldBe 1
    }

    @Test
    fun `활성 리뷰가 이미 있으면 중복 작성에 실패한다`() {
        every { catalogApi.assertProductExists(10L) } returns Unit
        every { reviewRepository.findByMemberIdAndProductId(1L, 10L) } returns review()

        val exception =
            shouldThrow<BusinessException> {
                reviewService.create(
                    memberId = 1L,
                    productId = 10L,
                    command = CreateReviewCommand(rating = 5, content = "중복 리뷰"),
                )
            }

        exception.errorCode shouldBe ReviewErrorCode.REVIEW_ALREADY_EXISTS
        verify(exactly = 0) { reviewRepository.save(any()) }
        verify(exactly = 0) { orderApi.findReviewablePurchase(any(), any()) }
    }

    @Test
    fun `삭제된 리뷰가 있으면 기존 리뷰를 복원해서 다시 작성한다`() {
        val deletedReview = review()
        deletedReview.delete(LocalDateTime.of(2026, 3, 21, 12, 30))

        every { catalogApi.assertProductExists(10L) } returns Unit
        every { reviewRepository.findByMemberIdAndProductId(1L, 10L) } returns deletedReview
        every { reviewRepository.save(deletedReview) } returns deletedReview

        val reviewId =
            reviewService.create(
                memberId = 1L,
                productId = 10L,
                command = CreateReviewCommand(rating = 3, content = "다시 작성"),
            )

        reviewId shouldBe 0L
        deletedReview.orderItemId shouldBe 100L
        deletedReview.rating shouldBe 3
        deletedReview.content shouldBe "다시 작성"
        deletedReview.deletedAt shouldBe null
        deletedReview.optionColor shouldBe "BLACK"
        deletedReview.optionSize shouldBe "M"
        verify(exactly = 0) { orderApi.findReviewablePurchase(any(), any()) }
    }

    @Test
    fun `구매 확정 주문상품이 없으면 리뷰 작성에 실패한다`() {
        every { catalogApi.assertProductExists(10L) } returns Unit
        every { reviewRepository.findByMemberIdAndProductId(1L, 10L) } returns null
        every { orderApi.findReviewablePurchase(1L, 10L) } returns emptyList()

        val exception =
            shouldThrow<BusinessException> {
                reviewService.create(
                    memberId = 1L,
                    productId = 10L,
                    command = CreateReviewCommand(rating = 5, content = "리뷰"),
                )
            }

        exception.errorCode shouldBe ReviewErrorCode.REVIEW_PURCHASE_REQUIRED
        verify(exactly = 0) { reviewRepository.save(any()) }
    }

    @Test
    fun `내 리뷰 조회는 최신 닉네임을 함께 반환한다`() {
        every { catalogApi.assertProductExists(10L) } returns Unit
        every { reviewRepository.findActiveByMemberIdAndProductId(1L, 10L) } returns review()
        every { memberApi.findProfileById(1L) } returns MemberProfile(1L, "latest-reviewer")

        val result = reviewService.getMyReview(memberId = 1L, productId = 10L)

        result.authorNickname shouldBe "latest-reviewer"
        result.optionColor shouldBe "BLACK"
        result.optionSize shouldBe "M"
    }

    @Test
    fun `리뷰 목록 조회 중 최신 닉네임을 찾지 못하면 실패한다`() {
        every { catalogApi.assertProductExists(10L) } returns Unit
        every {
            reviewRepository.findByProductId(
                productId = 10L,
                pageQuery = PageQuery(page = 0, size = 20),
            )
        } returns PageResult(
            items = listOf(review()),
            page = 0,
            size = 20,
            totalElements = 1,
            totalPages = 1,
        )
        every { memberApi.findProfilesByIds(setOf(1L)) } throws BusinessException(MemberErrorCode.MEMBER_NOT_FOUND)

        val exception =
            shouldThrow<BusinessException> {
                reviewService.getProductReviews(productId = 10L, page = 0, size = 20)
            }

        exception.errorCode shouldBe MemberErrorCode.MEMBER_NOT_FOUND
    }

    private fun review(): Review {
        return Review.create(
            memberId = 1L,
            productId = 10L,
            orderItemId = 100L,
            optionColor = "BLACK",
            optionSize = "M",
            rating = 5,
            content = "기존 리뷰",
            createdAt = LocalDateTime.of(2026, 3, 21, 12, 0),
        )
    }
}
