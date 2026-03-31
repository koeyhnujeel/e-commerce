package zoonza.commerce.cart.domain

import zoonza.commerce.shared.BusinessException
import java.time.LocalDateTime

class Cart(
    val id: Long = 0L,
    val memberId: Long,
    val items: MutableList<CartItem> = mutableListOf(),
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
    val version: Long? = null,
) {
    companion object {
        fun create(memberId: Long): Cart {
            require(memberId > 0) { "회원 ID는 1 이상이어야 합니다." }

            val now = LocalDateTime.now()
            return Cart(
                memberId = memberId,
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    fun addItem(
        productId: Long,
        productOptionId: Long,
        quantity: Long,
    ) {
        requirePositiveQuantity(quantity)

        val existingItem = items.firstOrNull { it.productOptionId == productOptionId }
        if (existingItem != null) {
            require(existingItem.productId == productId) { "상품 ID와 상품 옵션 ID 조합이 올바르지 않습니다." }
            existingItem.increaseQuantity(quantity)
            touch()
            return
        }

        items.add(CartItem.create(productId, productOptionId, quantity))
        touch()
    }

    fun changeQuantity(
        productOptionId: Long,
        quantity: Long,
    ) {
        requirePositiveQuantity(quantity)

        val existingItem = findItem(productOptionId)
        existingItem.changeQuantity(quantity)
        touch()
    }

    fun removeItem(productOptionId: Long) {
        val removed = items.removeIf { it.productOptionId == productOptionId }
        if (!removed) {
            throw BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND)
        }

        touch()
    }

    fun clear() {
        if (items.isEmpty()) {
            return
        }

        items.clear()
        touch()
    }

    private fun findItem(productOptionId: Long): CartItem {
        return items.firstOrNull { it.productOptionId == productOptionId }
            ?: throw BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND)
    }

    private fun requirePositiveQuantity(quantity: Long) {
        require(quantity > 0) { "수량은 1 이상이어야 합니다." }
    }

    private fun touch() {
        updatedAt = LocalDateTime.now()
    }
}
