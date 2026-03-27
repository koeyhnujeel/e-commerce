package zoonza.commerce.order.adapter.out.persistence

import jakarta.persistence.*
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

@Entity
@Table(
    name = "customer_order",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_customer_order_order_number",
            columnNames = ["order_number"],
        ),
    ],
)
class OrderJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long = 0,

    @Column(name = "order_number", nullable = false, length = 64)
    val orderNumber: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: OrderStatus = OrderStatus.CREATED,

    @Embedded
    @AttributeOverride(
        name = "amount",
        column = Column(name = "total_amount", nullable = false),
    )
    var totalAmount: Money = Money(0),

    @Column(name = "ordered_at", nullable = false)
    val orderedAt: LocalDateTime = LocalDateTime.MIN,

    @Column(name = "delivered_at")
    var deliveredAt: LocalDateTime? = null,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("id ASC")
    val items: MutableList<OrderItemJpaEntity> = mutableListOf(),
) {
    fun toDomain(): Order {
        val order =
            Order(
                id = id,
                memberId = memberId,
                orderNumber = orderNumber,
                status = status,
                totalAmount = totalAmount,
                orderedAt = orderedAt,
                deliveredAt = deliveredAt,
                deletedAt = deletedAt,
                items = mutableListOf(),
            )

        items.map(OrderItemJpaEntity::toDomain)
            .onEach { it.belongTo(order) }
            .forEach(order.items::add)

        return order
    }

    companion object {
        fun from(order: Order): OrderJpaEntity {
            val orderJpaEntity =
                OrderJpaEntity(
                    id = order.id,
                    memberId = order.memberId,
                    orderNumber = order.orderNumber,
                    status = order.status,
                    totalAmount = order.totalAmount,
                    orderedAt = order.orderedAt,
                    deliveredAt = order.deliveredAt,
                    deletedAt = order.deletedAt,
                )

            order.items
                .map { OrderItemJpaEntity.from(it, orderJpaEntity) }
                .forEach(orderJpaEntity.items::add)

            return orderJpaEntity
        }
    }
}
