package zoonza.commerce.like.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlin.test.Test
import java.time.LocalDateTime
import zoonza.commerce.exception.BusinessException
import zoonza.commerce.exception.ErrorCode
import zoonza.commerce.like.Like
import zoonza.commerce.like.LikeTargetType
import zoonza.commerce.like.port.out.LikeRepository
import zoonza.commerce.like.port.out.LikeTargetReader

class DefaultLikeServiceTest {
    private val likeRepository = mockk<LikeRepository>()
    private val likeTargetReader = mockk<LikeTargetReader>()
    private val likeService =
        DefaultLikeService(
            likeRepository = likeRepository,
            likeTargetReader = likeTargetReader,
        )

    @Test
    fun `신규 좋아요 요청이면 좋아요를 저장한다`() {
        val likeSlot = slot<Like>()

        every { likeTargetReader.exists(LikeTargetType.PRODUCT, 10L) } returns true
        every { likeRepository.findByMemberIdAndTarget(1L, LikeTargetType.PRODUCT, 10L) } returns null
        every { likeRepository.saveIfAbsent(capture(likeSlot)) } answers { firstArg() }

        likeService.like(
            memberId = 1L,
            targetType = LikeTargetType.PRODUCT,
            targetId = 10L,
        )

        verify(exactly = 1) { likeTargetReader.exists(LikeTargetType.PRODUCT, 10L) }
        verify(exactly = 1) { likeRepository.findByMemberIdAndTarget(1L, LikeTargetType.PRODUCT, 10L) }
        verify(exactly = 1) { likeRepository.saveIfAbsent(any()) }
        likeSlot.captured.memberId shouldBe 1L
        likeSlot.captured.targetType shouldBe LikeTargetType.PRODUCT
        likeSlot.captured.targetId shouldBe 10L
        likeSlot.captured.deletedAt.shouldBeNull()
    }

    @Test
    fun `이미 좋아요 상태면 멱등 성공한다`() {
        val existingLike =
            Like.create(
                id = 1L,
                memberId = 1L,
                targetType = LikeTargetType.PRODUCT,
                targetId = 10L,
                likedAt = LocalDateTime.of(2026, 3, 15, 12, 0),
            )

        every { likeTargetReader.exists(LikeTargetType.PRODUCT, 10L) } returns true
        every { likeRepository.findByMemberIdAndTarget(1L, LikeTargetType.PRODUCT, 10L) } returns existingLike

        likeService.like(
            memberId = 1L,
            targetType = LikeTargetType.PRODUCT,
            targetId = 10L,
        )

        verify(exactly = 0) { likeRepository.saveIfAbsent(any()) }
        verify(exactly = 0) { likeRepository.save(any()) }
    }

    @Test
    fun `취소된 좋아요가 있으면 복구한다`() {
        val originalLikedAt = LocalDateTime.of(2026, 3, 15, 12, 0)
        val deletedLike =
            Like.create(
                id = 1L,
                memberId = 1L,
                targetType = LikeTargetType.PRODUCT,
                targetId = 10L,
                likedAt = originalLikedAt,
                deletedAt = LocalDateTime.of(2026, 3, 15, 12, 30),
            )
        val likeSlot = slot<Like>()

        every { likeTargetReader.exists(LikeTargetType.PRODUCT, 10L) } returns true
        every { likeRepository.findByMemberIdAndTarget(1L, LikeTargetType.PRODUCT, 10L) } returns deletedLike
        every { likeRepository.save(capture(likeSlot)) } answers { firstArg() }

        likeService.like(
            memberId = 1L,
            targetType = LikeTargetType.PRODUCT,
            targetId = 10L,
        )

        verify(exactly = 1) { likeRepository.save(any()) }
        likeSlot.captured.id shouldBe 1L
        likeSlot.captured.deletedAt.shouldBeNull()
        likeSlot.captured.likedAt shouldNotBe originalLikedAt
    }

    @Test
    fun `좋아요 대상이 없으면 예외를 던진다`() {
        every { likeTargetReader.exists(LikeTargetType.PRODUCT, 10L) } returns false

        val exception =
            shouldThrow<BusinessException> {
                likeService.like(
                    memberId = 1L,
                    targetType = LikeTargetType.PRODUCT,
                    targetId = 10L,
                )
            }

        exception.errorCode shouldBe ErrorCode.PRODUCT_NOT_FOUND
        verify(exactly = 0) { likeRepository.findByMemberIdAndTarget(any(), any(), any()) }
    }

    @Test
    fun `좋아요를 취소하면 삭제 시각을 기록한다`() {
        val existingLike =
            Like.create(
                id = 1L,
                memberId = 1L,
                targetType = LikeTargetType.PRODUCT,
                targetId = 10L,
                likedAt = LocalDateTime.of(2026, 3, 15, 12, 0),
            )
        val likeSlot = slot<Like>()

        every { likeTargetReader.exists(LikeTargetType.PRODUCT, 10L) } returns true
        every { likeRepository.findByMemberIdAndTarget(1L, LikeTargetType.PRODUCT, 10L) } returns existingLike
        every { likeRepository.save(capture(likeSlot)) } answers { firstArg() }

        likeService.cancel(
            memberId = 1L,
            targetType = LikeTargetType.PRODUCT,
            targetId = 10L,
        )

        verify(exactly = 1) { likeRepository.save(any()) }
        likeSlot.captured.deletedAt.shouldNotBeNull()
        likeSlot.captured.likedAt shouldBe LocalDateTime.of(2026, 3, 15, 12, 0)
    }

    @Test
    fun `이미 취소된 좋아요면 멱등 성공한다`() {
        val deletedLike =
            Like.create(
                id = 1L,
                memberId = 1L,
                targetType = LikeTargetType.PRODUCT,
                targetId = 10L,
                likedAt = LocalDateTime.of(2026, 3, 15, 12, 0),
                deletedAt = LocalDateTime.of(2026, 3, 15, 12, 30),
            )

        every { likeTargetReader.exists(LikeTargetType.PRODUCT, 10L) } returns true
        every { likeRepository.findByMemberIdAndTarget(1L, LikeTargetType.PRODUCT, 10L) } returns deletedLike

        likeService.cancel(
            memberId = 1L,
            targetType = LikeTargetType.PRODUCT,
            targetId = 10L,
        )

        verify(exactly = 0) { likeRepository.save(any()) }
    }

    @Test
    fun `좋아요가 없어도 취소 요청은 멱등 성공한다`() {
        every { likeTargetReader.exists(LikeTargetType.PRODUCT, 10L) } returns true
        every { likeRepository.findByMemberIdAndTarget(1L, LikeTargetType.PRODUCT, 10L) } returns null

        likeService.cancel(
            memberId = 1L,
            targetType = LikeTargetType.PRODUCT,
            targetId = 10L,
        )

        verify(exactly = 0) { likeRepository.save(any()) }
    }
}
