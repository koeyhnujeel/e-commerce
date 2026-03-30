package zoonza.commerce.catalog.adapter.out.persistence.product

import jakarta.persistence.*
import zoonza.commerce.catalog.domain.product.ProductOption
import zoonza.commerce.shared.Money
import java.math.BigDecimal

@Entity
@Table(
    name = "product_option",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_product_option_product_id_option_color_option_size",
            columnNames = ["product_id", "option_color", "option_size"],
        ),
        UniqueConstraint(
            name = "uk_product_option_product_id_sort_order",
            columnNames = ["product_id", "sort_order"],
        ),
    ],
)
class ProductOptionJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "option_color", nullable = false)
    val color: String = "",

    @Column(name = "option_size", nullable = false)
    val size: String = "",

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int = 0,

    @Column(name = "additional_price", nullable = false, precision = 19, scale = 0)
    val additionalPrice: BigDecimal = BigDecimal.ZERO,
) {
    companion object {
        fun from(option: ProductOption): ProductOptionJpaEntity {
            return ProductOptionJpaEntity(
                id = option.id,
                color = option.color,
                size = option.size,
                sortOrder = option.sortOder,
                additionalPrice = option.additionalPrice.amount,
            )
        }
    }

    fun toDomain(): ProductOption {
        return ProductOption(
            id = id,
            color = color,
            size = size,
            sortOder = sortOrder,
            additionalPrice = Money(additionalPrice),
        )
    }
}
