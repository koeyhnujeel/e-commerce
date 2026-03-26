package zoonza.commerce.like.application.service

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import zoonza.commerce.like.ProductLikeCanceled
import zoonza.commerce.like.ProductLiked
import zoonza.commerce.like.application.port.out.LikeRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike

class DefaultLikeServiceTest {
    private val likeRepository = mockk<LikeRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val likeService = DefaultLikeService(likeRepository, eventPublisher)

    @Test
    fun `신규 좋아요 요청이면 좋아요를 저장한다`() {
        val savedMemberLike = slot<MemberLike>()
        every { likeRepository.findByMemberIdAndTargetId(1L, 10L, LikeTargetType.PRODUCT) } returns null
        every { likeRepository.save(capture(savedMemberLike)) } answers { savedMemberLike.captured }

        likeService.like(memberId = 1L, targetId = 10L, targetType = LikeTargetType.PRODUCT)

        savedMemberLike.captured.memberId shouldBe 1L
        savedMemberLike.captured.targetId shouldBe 10L
        savedMemberLike.captured.deletedAt.shouldBeNull()
        verify(exactly = 1) { eventPublisher.publishEvent(ProductLiked(productId = 10L)) }
    }

    @Test
    fun `취소된 좋아요가 있으면 복구한다`() {
        val existingMemberLike = MemberLike.create(memberId = 1L, targetId = 10L, targetType = LikeTargetType.PRODUCT)
        existingMemberLike.cancel()
        every { likeRepository.findByMemberIdAndTargetId(1L, 10L, LikeTargetType.PRODUCT) } returns existingMemberLike
        every { likeRepository.save(existingMemberLike) } returns existingMemberLike

        likeService.like(memberId = 1L, targetId = 10L, targetType = LikeTargetType.PRODUCT)

        existingMemberLike.deletedAt.shouldBeNull()
        verify(exactly = 1) { likeRepository.save(existingMemberLike) }
        verify(exactly = 1) { eventPublisher.publishEvent(ProductLiked(productId = 10L)) }
    }

    @Test
    fun `이미 활성 상태인 좋아요를 다시 요청해도 통계 이벤트는 발행하지 않는다`() {
        val existingMemberLike = MemberLike.create(memberId = 1L, targetId = 10L, targetType = LikeTargetType.PRODUCT)
        every { likeRepository.findByMemberIdAndTargetId(1L, 10L, LikeTargetType.PRODUCT) } returns existingMemberLike
        every { likeRepository.save(existingMemberLike) } returns existingMemberLike

        likeService.like(memberId = 1L, targetId = 10L, targetType = LikeTargetType.PRODUCT)

        verify(exactly = 0) { eventPublisher.publishEvent(any<ProductLiked>()) }
        verify(exactly = 0) { eventPublisher.publishEvent(any<ProductLikeCanceled>()) }
    }

    @Test
    fun `좋아요 취소 요청이면 삭제 시각을 기록한다`() {
        val existingMemberLike = MemberLike.create(memberId = 1L, targetId = 10L, targetType = LikeTargetType.PRODUCT)
        every { likeRepository.findByMemberIdAndTargetId(1L, 10L, LikeTargetType.PRODUCT) } returns existingMemberLike
        every { likeRepository.save(existingMemberLike) } returns existingMemberLike

        likeService.cancelLike(memberId = 1L, targetId = 10L, targetType = LikeTargetType.PRODUCT)

        existingMemberLike.deletedAt.shouldNotBeNull()
        verify(exactly = 1) { likeRepository.save(existingMemberLike) }
        verify(exactly = 1) { eventPublisher.publishEvent(ProductLikeCanceled(productId = 10L)) }
    }

    @Test
    fun `좋아요가 없어도 취소 요청은 멱등 성공한다`() {
        every { likeRepository.findByMemberIdAndTargetId(1L, 10L, LikeTargetType.PRODUCT) } returns null

        likeService.cancelLike(memberId = 1L, targetId = 10L, targetType = LikeTargetType.PRODUCT)

        verify(exactly = 0) { likeRepository.save(any()) }
        verify(exactly = 0) { eventPublisher.publishEvent(any<ProductLiked>()) }
        verify(exactly = 0) { eventPublisher.publishEvent(any<ProductLikeCanceled>()) }
    }
}
