package zoonza.commerce.order.application.dto

import zoonza.commerce.order.domain.OrderStatus
import java.time.LocalDateTime

data class PlaceOrderResult(
    val orderId: Long,
    val orderNumber: String,
    val status: OrderStatus,
    val expiresAt: LocalDateTime,
    val totalAmount: Long,
) {
    companion object {
        fun of(
            orderId: Long,
            orderNumber: String,
            status: OrderStatus,
            expiresAt: LocalDateTime,
            totalAmount: Long,
        ): PlaceOrderResult {
            return PlaceOrderResult(
                orderId = orderId,
                orderNumber = orderNumber,
                status = status,
                expiresAt = expiresAt,
                totalAmount = totalAmount,
            )
        }
    }
}
