package zoonza.commerce.like.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MemberLikeTest {
    @Test
    fun `좋아요를 생성한다`() {
        val memberLike = MemberLike.create(memberId = 1L, targetId = 10L, targetType = LikeTargetType.PRODUCT)

        memberLike.memberId shouldBe 1L
        memberLike.targetId shouldBe 10L
        memberLike.targetType shouldBe LikeTargetType.PRODUCT
        memberLike.deletedAt.shouldBeNull()
    }

    @Test
    fun `회원 ID가 0 이하면 예외를 던진다`() {
        shouldThrow<IllegalArgumentException> {
            MemberLike.create(memberId = 0L, targetId = 10L, targetType = LikeTargetType.PRODUCT)
        }
    }

    @Test
    fun `좋아요를 취소하면 삭제 시각을 기록한다`() {
        val memberLike = MemberLike.create(memberId = 1L, targetId = 10L, targetType = LikeTargetType.PRODUCT)

        memberLike.cancel()

        memberLike.deletedAt.shouldNotBeNull()
    }

    @Test
    fun `취소된 좋아요를 복구하면 삭제 시각을 지운다`() {
        val memberLike = MemberLike.create(memberId = 1L, targetId = 10L, targetType = LikeTargetType.PRODUCT)
        memberLike.cancel()

        memberLike.restore()

        memberLike.deletedAt.shouldBeNull()
    }
}
