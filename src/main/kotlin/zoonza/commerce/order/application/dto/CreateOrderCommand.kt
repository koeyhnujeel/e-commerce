package zoonza.commerce.order.application.dto

data class CreateOrderCommand(
    val items: List<CreateOrderItemCommand>,
)

data class CreateOrderItemCommand(
    val productId: Long,
    val productOptionId: Long,
    val quantity: Int,
)
