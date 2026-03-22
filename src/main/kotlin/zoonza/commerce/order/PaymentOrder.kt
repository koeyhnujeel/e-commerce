package zoonza.commerce.order

import zoonza.commerce.shared.Money

data class PaymentOrder(
    val orderId: Long,
    val memberId: Long,
    val orderNumber: String,
    val payable: Boolean,
    val totalAmount: Money,
    val productNames: List<String>,
)
