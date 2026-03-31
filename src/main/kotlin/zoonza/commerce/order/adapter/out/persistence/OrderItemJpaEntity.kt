package zoonza.commerce.order.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import zoonza.commerce.order.domain.OrderItem
import zoonza.commerce.shared.Money
import java.math.BigDecimal

@Entity
@Table(name = "orders_item")
class OrderItemJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "product_id", nullable = false)
    val productId: Long = 0L,

    @Column(name = "product_option_id", nullable = false)
    val productOptionId: Long = 0L,

    @Column(name = "product_name", nullable = false)
    val productName: String = "",

    @Column(name = "primary_image_url")
    val primaryImageUrl: String? = null,

    @Column(name = "option_color", nullable = false)
    val optionColor: String = "",

    @Column(name = "option_size", nullable = false)
    val optionSize: String = "",

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 0)
    val unitPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "quantity", nullable = false)
    val quantity: Long = 0L,
) {
    companion object {
        fun from(item: OrderItem): OrderItemJpaEntity {
            return OrderItemJpaEntity(
                productId = item.productId,
                productOptionId = item.productOptionId,
                productName = item.productName,
                primaryImageUrl = item.primaryImageUrl,
                optionColor = item.optionColor,
                optionSize = item.optionSize,
                unitPrice = item.unitPrice.amount,
                quantity = item.quantity,
            )
        }
    }

    fun toDomain(): OrderItem {
        return OrderItem(
            productId = productId,
            productOptionId = productOptionId,
            productName = productName,
            primaryImageUrl = primaryImageUrl,
            optionColor = optionColor,
            optionSize = optionSize,
            unitPrice = Money(unitPrice),
            quantity = quantity,
        )
    }
}
