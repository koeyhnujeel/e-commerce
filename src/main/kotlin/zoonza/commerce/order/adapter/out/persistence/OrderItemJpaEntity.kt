package zoonza.commerce.order.adapter.out.persistence

import jakarta.persistence.*
import zoonza.commerce.order.domain.OrderItem
import zoonza.commerce.order.domain.OrderItemStatus
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

@Entity
@Table(name = "order_item")
class OrderItemJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "product_id", nullable = false)
    val productId: Long = 0,

    @Column(name = "product_option_id", nullable = false)
    val productOptionId: Long = 0,

    @Column(name = "product_name_snapshot", nullable = false)
    val productNameSnapshot: String = "",

    @Column(name = "option_color_snapshot", nullable = false)
    var optionColorSnapshot: String = "",

    @Column(name = "option_size_snapshot", nullable = false)
    var optionSizeSnapshot: String = "",

    @Column(nullable = false)
    val quantity: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: OrderItemStatus = OrderItemStatus.CREATED,

    @Column(name = "confirmed_at")
    var confirmedAt: LocalDateTime? = null,

    @Embedded
    @AttributeOverride(
        name = "amount",
        column = Column(name = "order_price", nullable = false),
    )
    val orderPrice: Money = Money(0),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: OrderJpaEntity? = null,
) {
    fun toDomain(): OrderItem {
        return OrderItem(
            id = id,
            productId = productId,
            productOptionId = productOptionId,
            productNameSnapshot = productNameSnapshot,
            optionColorSnapshot = optionColorSnapshot,
            optionSizeSnapshot = optionSizeSnapshot,
            quantity = quantity,
            status = status,
            confirmedAt = confirmedAt,
            orderPrice = orderPrice,
        )
    }

    companion object {
        fun from(
            item: OrderItem,
            order: OrderJpaEntity,
        ): OrderItemJpaEntity {
            return OrderItemJpaEntity(
                id = item.id,
                productId = item.productId,
                productOptionId = item.productOptionId,
                productNameSnapshot = item.productNameSnapshot,
                optionColorSnapshot = item.optionColorSnapshot,
                optionSizeSnapshot = item.optionSizeSnapshot,
                quantity = item.quantity,
                status = item.status,
                confirmedAt = item.confirmedAt,
                orderPrice = item.orderPrice,
                order = order,
            )
        }
    }
}
