package zoonza.commerce.cart.domain

import java.time.LocalDateTime

class CartItem(
    val id: Long = 0L,
    val productId: Long,
    val productOptionId: Long,
    var quantity: Long,
    val addedAt: LocalDateTime,
) {
    companion object {
        fun create(
            productId: Long,
            productOptionId: Long,
            quantity: Long,
        ): CartItem {
            require(productId > 0) { "상품 ID는 1 이상이어야 합니다." }
            require(productOptionId > 0) { "상품 옵션 ID는 1 이상이어야 합니다." }
            require(quantity > 0) { "수량은 1 이상이어야 합니다." }

            return CartItem(
                productId = productId,
                productOptionId = productOptionId,
                quantity = quantity,
                addedAt = LocalDateTime.now(),
            )
        }
    }

    fun increaseQuantity(quantity: Long) {
        require(quantity > 0) { "수량은 1 이상이어야 합니다." }
        this.quantity += quantity
    }

    fun changeQuantity(quantity: Long) {
        require(quantity > 0) { "수량은 1 이상이어야 합니다." }
        this.quantity = quantity
    }
}
