package zoonza.commerce.order.adapter.`in`.response

import zoonza.commerce.order.domain.OrderItemStatus
import zoonza.commerce.order.domain.OrderStatus
import java.time.LocalDateTime

data class OrderSummaryResponse(
    val orderId: Long,
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: Long,
    val orderedAt: LocalDateTime,
)

data class OrderDetailResponse(
    val orderId: Long,
    val orderNumber: String,
    val status: OrderStatus,
    val totalAmount: Long,
    val orderedAt: LocalDateTime,
    val deliveredAt: LocalDateTime?,
    val items: List<OrderItemResponse>,
)

data class OrderItemResponse(
    val orderItemId: Long,
    val productId: Long,
    val productOptionId: Long,
    val productName: String,
    val optionColor: String,
    val optionSize: String,
    val orderPrice: Long,
    val quantity: Int,
    val lineAmount: Long,
    val status: OrderItemStatus,
    val confirmedAt: LocalDateTime?,
)
