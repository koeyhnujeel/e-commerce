package zoonza.commerce.adapter.out.persistence.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import zoonza.commerce.product.ProductOption

@Entity
@Table(
    name = "product_option",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_product_option_product_id_option_color_option_size",
            columnNames = ["product_id", "option_color", "option_size"],
        ),
        UniqueConstraint(
            name = "uk_product_option_product_id_stock_id",
            columnNames = ["product_id", "stock_id"],
        ),
    ],
)
class ProductOptionJpaEntity private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "option_color", nullable = false)
    val color: String,

    @Column(name = "option_size", nullable = false)
    val size: String,

    @Column(name = "stock_id", nullable = false)
    val stockId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: ProductJpaEntity,
) {
    companion object {
        fun from(
            option: ProductOption,
            product: ProductJpaEntity,
        ): ProductOptionJpaEntity {
            return ProductOptionJpaEntity(
                id = option.id,
                color = option.color,
                size = option.size,
                stockId = option.stockId,
                product = product,
            )
        }
    }

    fun toDomain(): ProductOption {
        return ProductOption.create(
            id = id,
            color = color,
            size = size,
            stockId = stockId,
        )
    }
}
