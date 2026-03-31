package zoonza.commerce.order.domain

import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class Order(
    val id: Long = 0L,
    val memberId: Long,
    val orderNumber: String,
    val source: OrderSource,
    var status: OrderStatus,
    val orderedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val recipient: OrderRecipient,
    val items: MutableList<OrderItem>,
    val totalAmount: Money,
    var canceledAt: LocalDateTime? = null,
    var expiredAt: LocalDateTime? = null,
    var paidAt: LocalDateTime? = null,
) {
    companion object {
        fun create(
            memberId: Long,
            orderNumber: String,
            source: OrderSource,
            orderedAt: LocalDateTime,
            expiresAt: LocalDateTime,
            recipient: OrderRecipient,
            items: List<OrderItem>,
        ): Order {
            require(memberId > 0) { "회원 ID는 1 이상이어야 합니다." }
            require(orderNumber.isNotBlank()) { "주문번호는 비어 있을 수 없습니다." }
            require(expiresAt.isAfter(orderedAt)) { "주문 만료 시간은 주문 생성 시간 이후여야 합니다." }
            require(items.isNotEmpty()) { "주문 항목은 최소 1개 이상이어야 합니다." }

            return Order(
                memberId = memberId,
                orderNumber = orderNumber.trim(),
                source = source,
                status = OrderStatus.PENDING_PAYMENT,
                orderedAt = orderedAt,
                expiresAt = expiresAt,
                recipient = recipient,
                items = items.toMutableList(),
                totalAmount = items.fold(Money(0)) { acc, item -> acc + item.lineAmount() },
            )
        }
    }

    fun cancel(canceledAt: LocalDateTime) {
        require(status == OrderStatus.PENDING_PAYMENT) { "취소할 수 없는 주문입니다." }

        status = OrderStatus.CANCELED
        this.canceledAt = canceledAt
    }

    fun expire(expiredAt: LocalDateTime) {
        require(status == OrderStatus.PENDING_PAYMENT) { "만료 처리할 수 없는 주문입니다." }

        status = OrderStatus.EXPIRED
        this.expiredAt = expiredAt
    }

    fun markPaid(paidAt: LocalDateTime) {
        require(status == OrderStatus.PENDING_PAYMENT) { "결제 처리할 수 없는 주문입니다." }

        status = OrderStatus.PAID
        this.paidAt = paidAt
    }
}
