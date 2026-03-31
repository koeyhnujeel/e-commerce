package zoonza.commerce.order.adapter.`in`.response

import zoonza.commerce.order.application.dto.PlaceOrderResult
import java.time.LocalDateTime

data class PlaceOrderResponse(
    val orderId: Long,
    val orderNumber: String,
    val status: String,
    val expiresAt: LocalDateTime,
    val totalAmount: Long,
) {
    companion object {
        fun from(result: PlaceOrderResult): PlaceOrderResponse {
            return PlaceOrderResponse(
                orderId = result.orderId,
                orderNumber = result.orderNumber,
                status = result.status.name,
                expiresAt = result.expiresAt,
                totalAmount = result.totalAmount,
            )
        }
    }
}
