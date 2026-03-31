package zoonza.commerce.cart.application.port.`in`

import zoonza.commerce.cart.application.dto.CartView

interface CartService {
    fun getMyCart(memberId: Long): CartView

    fun addItem(
        memberId: Long,
        productId: Long,
        productOptionId: Long,
        quantity: Long,
    )

    fun changeItemQuantity(
        memberId: Long,
        productOptionId: Long,
        quantity: Long,
    )

    fun removeItem(
        memberId: Long,
        productOptionId: Long,
    )

    fun clearCart(memberId: Long)
}
