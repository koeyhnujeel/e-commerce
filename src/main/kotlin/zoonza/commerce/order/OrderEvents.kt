package zoonza.commerce.order

data class OrderCreated(
    val orderId: Long,
    val orderNumber: String,
    val memberId: Long,
)

data class OrderCanceled(
    val orderId: Long,
    val orderNumber: String,
    val memberId: Long,
)

data class OrderExpired(
    val orderId: Long,
    val orderNumber: String,
    val memberId: Long,
)

data class OrderPaid(
    val orderId: Long,
    val orderNumber: String,
    val memberId: Long,
)
