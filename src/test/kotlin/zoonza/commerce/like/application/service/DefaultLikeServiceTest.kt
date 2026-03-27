package zoonza.commerce.like.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.like.application.dto.ProductLikeStatus
import zoonza.commerce.like.domain.LikeErrorCode
import zoonza.commerce.like.domain.LikeRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.ProductLiked
import zoonza.commerce.shared.ProductUnliked

class DefaultLikeServiceTest {
    private val catalogApi = mockk<CatalogApi>()
    private val likeRepository = mockk<LikeRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val likeService = DefaultLikeService(catalogApi, likeRepository, eventPublisher)

    @Test
    fun `신규 좋아요 요청이면 좋아요를 저장한다`() {
        val savedMemberLike = slot<MemberLike>()
        every { catalogApi.validateProductExists(10L) } returns Unit
        every { likeRepository.findByMemberIdAndTargetId(1L, 10L, LikeTargetType.PRODUCT) } returns null
        every { likeRepository.save(capture(savedMemberLike)) } answers { savedMemberLike.captured }

        likeService.likeProduct(memberId = 1L, targetId = 10L)

        savedMemberLike.captured.memberId shouldBe 1L
        savedMemberLike.captured.targetId shouldBe 10L
        savedMemberLike.captured.likeTargetType shouldBe LikeTargetType.PRODUCT
        savedMemberLike.captured.deletedAt.shouldBeNull()
        verify(exactly = 1) { catalogApi.validateProductExists(10L) }
        verify(exactly = 1) { likeRepository.save(any()) }
        verify(exactly = 1) { eventPublisher.publishEvent(ProductLiked(productId = 10L)) }
    }

    @Test
    fun `취소된 좋아요가 있으면 복구한다`() {
        val existingMemberLike = MemberLike.create(memberId = 1L, targetId = 10L, likeTargetType = LikeTargetType.PRODUCT)
        existingMemberLike.unlike()
        every { catalogApi.validateProductExists(10L) } returns Unit
        every { likeRepository.findByMemberIdAndTargetId(1L, 10L, LikeTargetType.PRODUCT) } returns existingMemberLike
        every { likeRepository.save(existingMemberLike) } returns existingMemberLike

        likeService.likeProduct(memberId = 1L, targetId = 10L)

        existingMemberLike.deletedAt.shouldBeNull()
        verify(exactly = 1) { catalogApi.validateProductExists(10L) }
        verify(exactly = 1) { likeRepository.save(existingMemberLike) }
        verify(exactly = 1) { eventPublisher.publishEvent(ProductLiked(productId = 10L)) }
    }

    @Test
    fun `좋아요 취소 요청이면 삭제 시각을 기록한다`() {
        val existingMemberLike = MemberLike.create(memberId = 1L, targetId = 10L, likeTargetType = LikeTargetType.PRODUCT)
        every { catalogApi.validateProductExists(10L) } returns Unit
        every { likeRepository.findByMemberIdAndTargetId(1L, 10L, LikeTargetType.PRODUCT) } returns existingMemberLike
        every { likeRepository.save(existingMemberLike) } returns existingMemberLike

        likeService.unlikeProduct(memberId = 1L, targetId = 10L)

        existingMemberLike.deletedAt.shouldNotBeNull()
        verify(exactly = 1) { catalogApi.validateProductExists(10L) }
        verify(exactly = 1) { likeRepository.save(existingMemberLike) }
        verify(exactly = 1) { eventPublisher.publishEvent(ProductUnliked(productId = 10L)) }
    }

    @Test
    fun `좋아요가 없으면 취소 요청은 예외를 던진다`() {
        every { catalogApi.validateProductExists(10L) } returns Unit
        every { likeRepository.findByMemberIdAndTargetId(1L, 10L, LikeTargetType.PRODUCT) } returns null

        val exception = shouldThrow<BusinessException> {
            likeService.unlikeProduct(memberId = 1L, targetId = 10L)
        }

        exception.errorCode shouldBe LikeErrorCode.LIKE_NOT_FOUND
        verify(exactly = 1) { catalogApi.validateProductExists(10L) }
        verify(exactly = 0) { likeRepository.save(any()) }
        verify(exactly = 0) { eventPublisher.publishEvent(any<ProductLiked>()) }
        verify(exactly = 0) { eventPublisher.publishEvent(any<ProductUnliked>()) }
    }

    @Test
    fun `상품 좋아요 여부 조회는 요청한 상품 순서대로 liked 값을 반환한다`() {
        every {
            likeRepository.findLikedProduct(
                memberId = 1L,
                targetIds = listOf(20L, 10L),
                likeTargetType = LikeTargetType.PRODUCT,
            )
        } returns listOf(20L)

        val result = likeService.getProductLikeStatuses(
            memberId = 1L,
            productIds = listOf(20L, 10L),
        )

        result shouldBe
            listOf(
                ProductLikeStatus(productId = 20L, liked = true),
                ProductLikeStatus(productId = 10L, liked = false),
            )
        verify(exactly = 0) { catalogApi.validateProductExists(any()) }
        verify(exactly = 0) { likeRepository.findByMemberIdAndTargetId(any(), any(), any()) }
    }
}
