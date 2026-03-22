package zoonza.commerce.order

import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.shared.Money

data class PaymentOrder(
    val orderId: Long,
    val memberId: Long,
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: Money,
    val productNames: List<String>,
)
