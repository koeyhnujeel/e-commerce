package zoonza.commerce.catalog.adapter.out.persistence.statistic

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import zoonza.commerce.catalog.domain.statistic.ProductStatistic

@Entity
@Table(name = "product_statistic")
class ProductStatisticJpaEntity(
    @Id
    @Column(name = "product_id", nullable = false)
    val productId: Long = 0,

    @Column(name = "like_count", nullable = false)
    val likeCount: Long = 0,
) {
    companion object {
        fun from(productStatistic: ProductStatistic): ProductStatisticJpaEntity {
            return ProductStatisticJpaEntity(
                productId = productStatistic.productId,
                likeCount = productStatistic.likeCount,
            )
        }
    }

    fun toDomain(): ProductStatistic {
        return ProductStatistic.create(
            productId = productId,
            likeCount = likeCount,
        )
    }
}
