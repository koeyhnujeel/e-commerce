package zoonza.commerce.cart.domain

interface CartRepository {
    fun findByMemberId(memberId: Long): Cart?

    fun save(cart: Cart): Cart
}
