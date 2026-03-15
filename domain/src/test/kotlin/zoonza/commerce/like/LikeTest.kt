package zoonza.commerce.like

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import java.time.LocalDateTime

class LikeTest {
    @Test
    fun `좋아요를 생성한다`() {
        val likedAt = LocalDateTime.of(2026, 3, 15, 12, 0)

        val like =
            Like.create(
                memberId = 1L,
                targetType = LikeTargetType.PRODUCT,
                targetId = 10L,
                likedAt = likedAt,
            )

        like.memberId shouldBe 1L
        like.targetType shouldBe LikeTargetType.PRODUCT
        like.targetId shouldBe 10L
        like.likedAt shouldBe likedAt
        like.deletedAt.shouldBeNull()
        like.isDeleted() shouldBe false
    }

    @Test
    fun `좋아요를 취소하면 삭제 시각이 기록된다`() {
        val like =
            Like.create(
                memberId = 1L,
                targetType = LikeTargetType.PRODUCT,
                targetId = 10L,
                likedAt = LocalDateTime.of(2026, 3, 15, 12, 0),
            )
        val deletedAt = LocalDateTime.of(2026, 3, 15, 12, 30)

        like.cancel(deletedAt)

        like.deletedAt shouldBe deletedAt
        like.isDeleted() shouldBe true
    }

    @Test
    fun `취소된 좋아요를 복구하면 likedAt을 갱신하고 삭제 시각이 비워진다`() {
        val like =
            Like.create(
                memberId = 1L,
                targetType = LikeTargetType.PRODUCT,
                targetId = 10L,
                likedAt = LocalDateTime.of(2026, 3, 15, 12, 0),
                deletedAt = LocalDateTime.of(2026, 3, 15, 12, 30),
            )
        val restoredAt = LocalDateTime.of(2026, 3, 15, 13, 0)

        like.restore(restoredAt)

        like.likedAt shouldBe restoredAt
        like.deletedAt.shouldBeNull()
        like.isDeleted() shouldBe false
    }

    @Test
    fun `회원 ID는 1 이상이어야 한다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                Like.create(
                    memberId = 0L,
                    targetType = LikeTargetType.PRODUCT,
                    targetId = 10L,
                    likedAt = LocalDateTime.now(),
                )
            }

        exception.message shouldBe "회원 ID는 1 이상이어야 합니다."
    }

    @Test
    fun `좋아요 대상 ID는 1 이상이어야 한다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                Like.create(
                    memberId = 1L,
                    targetType = LikeTargetType.PRODUCT,
                    targetId = 0L,
                    likedAt = LocalDateTime.now(),
                )
            }

        exception.message shouldBe "좋아요 대상 ID는 1 이상이어야 합니다."
    }
}
