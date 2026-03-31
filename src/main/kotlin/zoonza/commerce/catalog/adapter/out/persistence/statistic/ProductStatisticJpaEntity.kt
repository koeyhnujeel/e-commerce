package zoonza.commerce.catalog.adapter.out.persistence.statistic

import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate
import zoonza.commerce.catalog.domain.statistic.ProductStatistic

@Entity
@DynamicUpdate
@Table(name = "product_statistic")
class ProductStatisticJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "product_id", nullable = false)
    val productId: Long = 0,

    @Column(name = "like_count", nullable = false)
    var likeCount: Long = 0,

    @Version
    @Column(name = "version", nullable = false)
    var version: Long? = null,
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
            id = this.id,
            productId = this.productId,
            likeCount = this.likeCount,
        )
    }

    fun updateFrom(productStatistic: ProductStatistic) {
        this.likeCount = productStatistic.likeCount
    }
}
