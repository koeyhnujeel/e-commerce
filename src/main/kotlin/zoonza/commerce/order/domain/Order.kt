package zoonza.commerce.order.domain

import jakarta.persistence.AttributeOverride
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
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
class Order private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "order_number", nullable = false, length = 64)
    val orderNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: OrderStatus,

    @Embedded
    @AttributeOverride(
        name = "amount",
        column = Column(name = "total_amount", nullable = false),
    )
    var totalAmount: Money,

    @Column(name = "ordered_at", nullable = false)
    val orderedAt: LocalDateTime,

    @Column(name = "delivered_at")
    var deliveredAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<OrderItem>,
) {
    companion object {
        fun create(
            memberId: Long,
            orderNumber: String,
            orderedAt: LocalDateTime,
            items: List<OrderItem>,
            status: OrderStatus = OrderStatus.CREATED,
            deliveredAt: LocalDateTime? = null,
            id: Long = 0,
        ): Order {
            require(id >= 0) { "주문 ID는 0 이상이어야 합니다." }
            require(memberId > 0) { "회원 ID는 1 이상이어야 합니다." }
            require(orderNumber.isNotBlank()) { "주문 번호는 비어 있을 수 없습니다." }
            require(items.isNotEmpty()) { "주문상품은 최소 1개 이상이어야 합니다." }
            require(status != OrderStatus.DELIVERED || deliveredAt != null) {
                "배송 완료 주문은 deliveredAt이 필요합니다."
            }

            val order = Order(
                id = id,
                memberId = memberId,
                orderNumber = orderNumber.trim(),
                status = status,
                totalAmount = Money(0),
                orderedAt = orderedAt,
                deliveredAt = deliveredAt,
                items = mutableListOf(),
            )

            items.onEach { it.belongTo(order) }
                .onEach { it.updateStatus(itemStatusOf(status)) }
                .forEach(order.items::add)
            order.totalAmount = order.calculateTotalAmount()

            return order
        }

        private fun itemStatusOf(status: OrderStatus): OrderItemStatus {
            return when (status) {
                OrderStatus.CREATED -> OrderItemStatus.CREATED
                OrderStatus.PAID -> OrderItemStatus.PAID
                OrderStatus.DELIVERED -> OrderItemStatus.DELIVERED
                OrderStatus.CANCELED -> OrderItemStatus.CANCELED
            }
        }
    }

    fun confirmPurchase(
        orderItemId: Long,
        optionColor: String,
        optionSize: String,
        confirmedAt: LocalDateTime,
    ) {
        val item = items.firstOrNull { it.id == orderItemId }
            ?: throw IllegalArgumentException("주문에 해당 주문상품이 없습니다.")

        item.confirmPurchase(
            color = optionColor,
            size = optionSize,
            confirmedAt = confirmedAt,
        )
    }

    private fun calculateTotalAmount(): Money {
        return items.fold(Money(0)) { acc, item -> acc + item.lineAmount() }
    }
}
