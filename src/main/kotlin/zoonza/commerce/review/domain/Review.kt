package zoonza.commerce.review.domain

import java.time.LocalDateTime

class Review(
    val id: Long = 0,
    val memberId: Long,
    val productId: Long,
    val orderItemId: Long,
    val optionColor: String,
    val optionSize: String,
    var rating: Int,
    var content: String,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
    var deletedAt: LocalDateTime? = null,
) {
    companion object {
        fun create(
            memberId: Long,
            productId: Long,
            orderItemId: Long,
            optionColor: String,
            optionSize: String,
            rating: Int,
            content: String,
            createdAt: LocalDateTime,
            id: Long = 0,
        ): Review {
            require(id >= 0) { "리뷰 ID는 0 이상이어야 합니다." }
            require(memberId > 0) { "회원 ID는 1 이상이어야 합니다." }
            require(productId > 0) { "상품 ID는 1 이상이어야 합니다." }
            require(orderItemId > 0) { "주문상품 ID는 1 이상이어야 합니다." }

            return Review(
                id = id,
                memberId = memberId,
                productId = productId,
                orderItemId = orderItemId,
                optionColor = normalizeOption(optionColor),
                optionSize = normalizeOption(optionSize),
                rating = validateRating(rating),
                content = normalizeContent(content),
                createdAt = createdAt,
                updatedAt = createdAt,
            )
        }

        private fun normalizeOption(option: String): String {
            require(option.isNotBlank()) { "리뷰 옵션은 비어 있을 수 없습니다." }
            return option.trim()
        }

        private fun validateRating(rating: Int): Int {
            require(rating in 1..5) { "리뷰 평점은 1점 이상 5점 이하여야 합니다." }
            return rating
        }

        private fun normalizeContent(content: String): String {
            require(content.isNotBlank()) { "리뷰 내용은 비어 있을 수 없습니다." }
            return content.trim()
        }
    }

    fun update(
        rating: Int,
        content: String,
        updatedAt: LocalDateTime,
    ) {
        this.rating = validateRating(rating)
        this.content = normalizeContent(content)
        this.updatedAt = updatedAt
    }

    fun delete(deletedAt: LocalDateTime) {
        this.deletedAt = deletedAt
        this.updatedAt = deletedAt
    }

    fun restore(
        rating: Int,
        content: String,
        restoredAt: LocalDateTime,
    ) {
        this.rating = validateRating(rating)
        this.content = normalizeContent(content)
        this.updatedAt = restoredAt
        this.deletedAt = null
    }
}
