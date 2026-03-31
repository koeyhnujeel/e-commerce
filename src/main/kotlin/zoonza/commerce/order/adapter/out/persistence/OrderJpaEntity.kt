package zoonza.commerce.order.adapter.out.persistence

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderSource
import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.shared.Money
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "orders",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_orders_order_number",
            columnNames = ["order_number"],
        ),
    ],
)
class OrderJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false)
    var memberId: Long = 0L,

    @Column(name = "order_number", nullable = false)
    var orderNumber: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "order_source", nullable = false, length = 50)
    var source: OrderSource = OrderSource.DIRECT_BUY,

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 50)
    var status: OrderStatus = OrderStatus.PENDING_PAYMENT,

    @Column(name = "ordered_at", nullable = false)
    var orderedAt: LocalDateTime = LocalDateTime.MIN,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime = LocalDateTime.MIN,

    @Embedded
    var recipient: OrderRecipientEmbeddable = OrderRecipientEmbeddable(),

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 0)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "canceled_at")
    var canceledAt: LocalDateTime? = null,

    @Column(name = "expired_at")
    var expiredAt: LocalDateTime? = null,

    @Column(name = "paid_at")
    var paidAt: LocalDateTime? = null,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    @OrderBy("id ASC")
    val items: MutableList<OrderItemJpaEntity> = mutableListOf(),
) {
    companion object {
        fun from(order: Order): OrderJpaEntity {
            return OrderJpaEntity(
                id = order.id,
                memberId = order.memberId,
                orderNumber = order.orderNumber,
                source = order.source,
                status = order.status,
                orderedAt = order.orderedAt,
                expiresAt = order.expiresAt,
                recipient = OrderRecipientEmbeddable.from(order.recipient),
                totalAmount = order.totalAmount.amount,
                canceledAt = order.canceledAt,
                expiredAt = order.expiredAt,
                paidAt = order.paidAt,
                items = order.items.map(OrderItemJpaEntity::from).toMutableList(),
            )
        }
    }

    fun toDomain(): Order {
        return Order(
            id = id,
            memberId = memberId,
            orderNumber = orderNumber,
            source = source,
            status = status,
            orderedAt = orderedAt,
            expiresAt = expiresAt,
            recipient = recipient.toDomain(),
            items = items.map(OrderItemJpaEntity::toDomain).toMutableList(),
            totalAmount = Money(totalAmount),
            canceledAt = canceledAt,
            expiredAt = expiredAt,
            paidAt = paidAt,
        )
    }

    fun updateFrom(order: Order) {
        memberId = order.memberId
        orderNumber = order.orderNumber
        source = order.source
        status = order.status
        orderedAt = order.orderedAt
        expiresAt = order.expiresAt
        recipient = OrderRecipientEmbeddable.from(order.recipient)
        totalAmount = order.totalAmount.amount
        canceledAt = order.canceledAt
        expiredAt = order.expiredAt
        paidAt = order.paidAt
        items.clear()
        items.addAll(order.items.map(OrderItemJpaEntity::from))
    }
}
