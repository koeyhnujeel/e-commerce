package zoonza.commerce.order.application.dto

data class CreateOrderResult(
    val orderId: Long,
    val orderNumber: String,
    val totalAmount: Long,
)
