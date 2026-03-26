package zoonza.commerce.catalog.domain.statistic

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "product_statistic")
class ProductStatistic private constructor(
    @Id
    @Column(name = "product_id", nullable = false)
    val productId: Long,

    @Column(name = "like_count", nullable = false)
    var likeCount: Long,
) {
    companion object {
        fun create(
            productId: Long,
            likeCount: Long = 0L,
        ): ProductStatistic {
            require(productId > 0) { "상품 ID는 1 이상이어야 합니다." }
            require(likeCount >= 0) { "좋아요 수는 0 이상이어야 합니다." }

            return ProductStatistic(
                productId = productId,
                likeCount = likeCount,
            )
        }
    }

    fun applyLikeCountDelta(delta: Long) {
        this.likeCount = (likeCount + delta).coerceAtLeast(0L)
    }
}