package zoonza.commerce.order.application.dto

data class UpdateOrderCommand(
    val items: List<UpdateOrderItemCommand>,
)

data class UpdateOrderItemCommand(
    val productId: Long,
    val productOptionId: Long,
    val quantity: Int,
)
