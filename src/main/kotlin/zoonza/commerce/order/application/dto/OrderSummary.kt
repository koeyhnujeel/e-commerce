package zoonza.commerce.order.application.dto

import zoonza.commerce.order.domain.OrderStatus
import java.time.LocalDateTime

data class OrderSummary(
    val orderId: Long,
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: Long,
    val orderedAt: LocalDateTime,
)
