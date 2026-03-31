package zoonza.commerce.cart

data class CartOrderItem(
    val productId: Long,
    val productOptionId: Long,
    val quantity: Long,
)
