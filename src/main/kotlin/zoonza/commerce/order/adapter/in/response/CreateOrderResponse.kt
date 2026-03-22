package zoonza.commerce.order.adapter.`in`.response

data class CreateOrderResponse(
    val orderId: Long,
    val orderNumber: String,
    val totalAmount: Long,
)
