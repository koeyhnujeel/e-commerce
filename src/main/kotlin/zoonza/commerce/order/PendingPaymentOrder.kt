package zoonza.commerce.order

import java.time.LocalDateTime

data class PendingPaymentOrder(
    val orderId: Long,
    val orderNumber: String,
    val memberId: Long,
    val totalAmount: Long,
    val expiresAt: LocalDateTime,
    val productNames: List<String>,
)
