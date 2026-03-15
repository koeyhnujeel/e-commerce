package zoonza.commerce.adapter.out.persistence.like

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import java.time.LocalDateTime
import zoonza.commerce.like.Like
import zoonza.commerce.like.LikeTargetType

class LikeJpaEntityTest {
    @Test
    fun `도메인 좋아요를 JPA 엔티티로 변환한다`() {
        val likedAt = LocalDateTime.of(2026, 3, 15, 12, 0)
        val deletedAt = LocalDateTime.of(2026, 3, 15, 12, 30)
        val like =
            Like.create(
                id = 1L,
                memberId = 2L,
                targetType = LikeTargetType.PRODUCT,
                targetId = 10L,
                likedAt = likedAt,
                deletedAt = deletedAt,
            )

        val entity = LikeJpaEntity.from(like)

        entity.id shouldBe 1L
        entity.memberId shouldBe 2L
        entity.targetType shouldBe LikeTargetType.PRODUCT
        entity.targetId shouldBe 10L
        entity.likedAt shouldBe likedAt
        entity.deletedAt shouldBe deletedAt
    }

    @Test
    fun `JPA 엔티티 좋아요를 도메인 모델로 변환한다`() {
        val likedAt = LocalDateTime.of(2026, 3, 15, 12, 0)
        val entity =
            LikeJpaEntity.from(
                Like.create(
                    id = 1L,
                    memberId = 2L,
                    targetType = LikeTargetType.PRODUCT,
                    targetId = 10L,
                    likedAt = likedAt,
                ),
            )

        val like = entity.toDomain()

        like.id shouldBe 1L
        like.memberId shouldBe 2L
        like.targetType shouldBe LikeTargetType.PRODUCT
        like.targetId shouldBe 10L
        like.likedAt shouldBe likedAt
        like.deletedAt.shouldBeNull()
    }
}
