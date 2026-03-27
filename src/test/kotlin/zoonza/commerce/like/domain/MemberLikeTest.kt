package zoonza.commerce.like.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class MemberLikeTest {
    @Test
    fun `좋아요를 생성한다`() {
        val memberLike = MemberLike.create(memberId = 1L, targetId = 10L, likeTargetType = LikeTargetType.PRODUCT)

        memberLike.memberId shouldBe 1L
        memberLike.targetId shouldBe 10L
        memberLike.likeTargetType shouldBe LikeTargetType.PRODUCT
        memberLike.deletedAt.shouldBeNull()
    }

    @Test
    fun `영속화된 좋아요를 복원한다`() {
        val likedAt = LocalDateTime.of(2026, 3, 27, 10, 0)
        val deletedAt = LocalDateTime.of(2026, 3, 27, 11, 0)

        val memberLike = MemberLike(
            id = 1L,
            memberId = 2L,
            targetId = 3L,
            likeTargetType = LikeTargetType.PRODUCT,
            likedAt = likedAt,
            deletedAt = deletedAt,
        )

        memberLike.id shouldBe 1L
        memberLike.memberId shouldBe 2L
        memberLike.targetId shouldBe 3L
        memberLike.likeTargetType shouldBe LikeTargetType.PRODUCT
        memberLike.likedAt shouldBe likedAt
        memberLike.deletedAt shouldBe deletedAt
    }

    @Test
    fun `회원 ID가 0 이하면 예외를 던진다`() {
        shouldThrow<IllegalArgumentException> {
            MemberLike.create(memberId = 0L, targetId = 10L, likeTargetType = LikeTargetType.PRODUCT)
        }
    }

    @Test
    fun `좋아요 대상 ID가 0 이하면 예외를 던진다`() {
        shouldThrow<IllegalArgumentException> {
            MemberLike.create(memberId = 1L, targetId = 0L, likeTargetType = LikeTargetType.PRODUCT)
        }
    }

    @Test
    fun `좋아요를 취소하면 삭제 시각을 기록한다`() {
        val memberLike = MemberLike.create(memberId = 1L, targetId = 10L, likeTargetType = LikeTargetType.PRODUCT)

        memberLike.unlike()

        memberLike.deletedAt.shouldNotBeNull()
    }

    @Test
    fun `취소된 좋아요를 복구하면 삭제 시각을 지운다`() {
        val memberLike = MemberLike.create(memberId = 1L, targetId = 10L, likeTargetType = LikeTargetType.PRODUCT)
        memberLike.unlike()

        memberLike.like()

        memberLike.deletedAt.shouldBeNull()
    }
}
