package zoonza.commerce.review.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ReviewTest {
    @Test
    fun `리뷰를 생성하면 내용과 옵션을 정규화한다`() {
        val createdAt = LocalDateTime.of(2026, 3, 21, 12, 0)

        val review = Review.create(
            memberId = 1L,
            productId = 10L,
            orderItemId = 100L,
            optionColor = "  BLACK  ",
            optionSize = "  M  ",
            rating = 5,
            content = "  만족합니다.  ",
            createdAt = createdAt,
        )

        review.optionColor shouldBe "BLACK"
        review.optionSize shouldBe "M"
        review.content shouldBe "만족합니다."
        review.updatedAt shouldBe createdAt
    }

    @Test
    fun `리뷰 삭제 후 복원하면 기존 주문상품 식별자와 옵션을 유지한다`() {
        val review = review()
        review.delete(LocalDateTime.of(2026, 3, 21, 13, 0))

        review.restore(
            rating = 4,
            content = "다시 작성한 리뷰",
            restoredAt = LocalDateTime.of(2026, 3, 21, 14, 0),
        )

        review.orderItemId shouldBe 100L
        review.optionColor shouldBe "BLACK"
        review.optionSize shouldBe "M"
        review.rating shouldBe 4
        review.content shouldBe "다시 작성한 리뷰"
        review.deletedAt shouldBe null
    }

    @Test
    fun `리뷰 평점은 1점 이상 5점 이하여야 한다`() {
        shouldThrow<IllegalArgumentException> {
            review(rating = 0)
        }
    }

    private fun review(rating: Int = 5): Review {
        return Review.create(
            memberId = 1L,
            productId = 10L,
            orderItemId = 100L,
            optionColor = "BLACK",
            optionSize = "M",
            rating = rating,
            content = "좋아요",
            createdAt = LocalDateTime.of(2026, 3, 21, 12, 0),
        )
    }
}
