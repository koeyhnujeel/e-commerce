package zoonza.commerce.order

interface OrderApi {
    fun findReviewablePurchase(
        memberId: Long,
        productId: Long,
    ): List<ReviewablePurchase>
}
