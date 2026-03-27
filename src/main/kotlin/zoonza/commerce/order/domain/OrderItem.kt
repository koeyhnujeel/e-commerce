package zoonza.commerce.order.domain

import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class OrderItem(
    val id: Long = 0,
    val productId: Long,
    val productOptionId: Long,
    val productNameSnapshot: String,
    var optionColorSnapshot: String,
    var optionSizeSnapshot: String,
    val quantity: Int,
    var status: OrderItemStatus,
    var confirmedAt: LocalDateTime? = null,
    val orderPrice: Money,
    var order: Order? = null,
) {
    companion object {
        fun create(
            productId: Long,
            productOptionId: Long,
            productNameSnapshot: String,
            optionColorSnapshot: String,
            optionSizeSnapshot: String,
            quantity: Int,
            orderPrice: Money,
            status: OrderItemStatus = OrderItemStatus.CREATED,
            id: Long = 0,
        ): OrderItem {
            require(id >= 0) { "주문상품 ID는 0 이상이어야 합니다." }
            require(productId > 0) { "상품 ID는 1 이상이어야 합니다." }
            require(productOptionId > 0) { "상품 옵션 ID는 1 이상이어야 합니다." }
            require(quantity > 0) { "주문 수량은 1 이상이어야 합니다." }

            return OrderItem(
                id = id,
                productId = productId,
                productOptionId = productOptionId,
                productNameSnapshot = normalizeSnapshot(productNameSnapshot, "상품명"),
                optionColorSnapshot = normalizeSnapshot(optionColorSnapshot, "옵션 색상"),
                optionSizeSnapshot = normalizeSnapshot(optionSizeSnapshot, "옵션 사이즈"),
                quantity = quantity,
                status = status,
                orderPrice = orderPrice,
            )
        }

        private fun normalizeSnapshot(
            value: String,
            label: String,
        ): String {
            require(value.isNotBlank()) { "주문상품 $label 스냅샷은 비어 있을 수 없습니다." }
            return value.trim()
        }
    }

    fun updateStatus(status: OrderItemStatus) {
        this.status = status
    }

    fun confirmPurchase(
        color: String,
        size: String,
        confirmedAt: LocalDateTime,
    ) {
        require(status == OrderItemStatus.DELIVERED) { "배송 완료된 주문상품만 구매 확정할 수 있습니다." }

        this.status = OrderItemStatus.PURCHASE_CONFIRMED
        this.confirmedAt = confirmedAt
        this.optionColorSnapshot = normalizeOption(color)
        this.optionSizeSnapshot = normalizeOption(size)
    }

    fun lineAmount(): Money {
        return orderPrice.multiply(quantity)
    }

    private fun normalizeOption(value: String): String {
        require(value.isNotBlank()) { "주문상품 옵션 스냅샷은 비어 있을 수 없습니다." }
        return value.trim()
    }

    internal fun belongTo(order: Order) {
        this.order = order
    }
}
