package zoonza.commerce.cart

interface CartApi {
    fun getSelectedItems(
        memberId: Long,
        productOptionIds: Set<Long>,
    ): List<CartOrderItem>

    fun removeItems(
        memberId: Long,
        productOptionIds: Set<Long>,
    )
}
