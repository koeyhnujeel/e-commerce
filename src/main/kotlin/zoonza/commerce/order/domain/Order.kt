package zoonza.commerce.order.domain

import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class Order(
    val id: Long = 0,
    val memberId: Long,
    val orderNumber: String,
    var status: OrderStatus,
    var totalAmount: Money,
    val orderedAt: LocalDateTime,
    var deliveredAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null,
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
            deletedAt: LocalDateTime? = null,
            id: Long = 0,
        ): Order {
            require(id >= 0) { "주문 ID는 0 이상이어야 합니다." }
            require(memberId > 0) { "회원 ID는 1 이상이어야 합니다." }
            require(orderNumber.isNotBlank()) { "주문 번호는 비어 있을 수 없습니다." }
            require(items.isNotEmpty()) { "주문상품은 최소 1개 이상이어야 합니다." }
            require(status != OrderStatus.DELIVERED || deliveredAt != null) {
                "배송 완료 주문은 deliveredAt이 필요합니다."
            }
            require(deletedAt == null || status == OrderStatus.CANCELED) {
                "삭제된 주문은 취소 상태여야 합니다."
            }

            val order = Order(
                id = id,
                memberId = memberId,
                orderNumber = orderNumber.trim(),
                status = status,
                totalAmount = Money(0),
                orderedAt = orderedAt,
                deliveredAt = deliveredAt,
                deletedAt = deletedAt,
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
                OrderStatus.PAYMENT_PENDING -> OrderItemStatus.PAYMENT_PENDING
                OrderStatus.PAID -> OrderItemStatus.PAID
                OrderStatus.DELIVERED -> OrderItemStatus.DELIVERED
                OrderStatus.CANCELED -> OrderItemStatus.CANCELED
            }
        }
    }

    fun canModify(): Boolean {
        return !isDeleted() && (status == OrderStatus.CREATED || status == OrderStatus.PAYMENT_PENDING)
    }

    fun canDelete(): Boolean {
        return !isDeleted() && (status == OrderStatus.CREATED || status == OrderStatus.PAYMENT_PENDING)
    }

    fun isDeleted(): Boolean {
        return deletedAt != null
    }

    fun replaceItems(items: List<OrderItem>) {
        require(items.isNotEmpty()) { "주문상품은 최소 1개 이상이어야 합니다." }
        require(canModify()) { "수정할 수 없는 주문입니다." }

        this.items.clear()
        items.onEach { it.belongTo(this) }
            .onEach { it.updateStatus(itemStatusOf(status)) }
            .forEach(this.items::add)
        this.totalAmount = calculateTotalAmount()
    }

    fun markPaymentPending() {
        require(!isDeleted()) { "삭제된 주문은 결제를 진행할 수 없습니다." }
        require(status == OrderStatus.CREATED) { "결제 대기 상태로 변경할 수 없는 주문입니다." }

        status = OrderStatus.PAYMENT_PENDING
        synchronizeItemStatuses()
    }

    fun markCreated() {
        require(!isDeleted()) { "삭제된 주문은 상태를 복구할 수 없습니다." }
        require(status == OrderStatus.PAYMENT_PENDING) { "결제 가능 상태로 복구할 수 없는 주문입니다." }

        status = OrderStatus.CREATED
        synchronizeItemStatuses()
    }

    fun markPaid() {
        require(!isDeleted()) { "삭제된 주문은 결제 완료 처리할 수 없습니다." }
        require(status == OrderStatus.PAYMENT_PENDING) { "결제 완료 처리할 수 없는 주문입니다." }

        status = OrderStatus.PAID
        synchronizeItemStatuses()
    }

    fun markDelivered(deliveredAt: LocalDateTime) {
        require(!isDeleted()) { "삭제된 주문은 배송 완료 처리할 수 없습니다." }
        require(status == OrderStatus.PAID) { "배송 완료 처리할 수 없는 주문입니다." }

        status = OrderStatus.DELIVERED
        this.deliveredAt = deliveredAt
        synchronizeItemStatuses()
    }

    fun cancel() {
        require(!isDeleted()) { "삭제된 주문은 취소할 수 없습니다." }
        require(
            status == OrderStatus.CREATED ||
                status == OrderStatus.PAYMENT_PENDING ||
                status == OrderStatus.PAID,
        ) { "취소할 수 없는 주문입니다." }

        status = OrderStatus.CANCELED
        deliveredAt = null
        synchronizeItemStatuses()
    }

    fun delete(deletedAt: LocalDateTime) {
        require(canDelete()) { "삭제할 수 없는 주문입니다." }

        cancel()
        this.deletedAt = deletedAt
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

    private fun synchronizeItemStatuses() {
        val itemStatus = itemStatusOf(status)
        items.filter { it.status != OrderItemStatus.PURCHASE_CONFIRMED }
            .forEach { it.updateStatus(itemStatus) }
    }
}
