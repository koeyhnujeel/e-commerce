package zoonza.commerce.order

interface OrderApi {
    fun findReviewablePurchase(
        memberId: Long,
        productId: Long,
    ): List<ReviewablePurchase>

    fun getPaymentOrder(
        memberId: Long,
        orderId: Long,
    ): PaymentOrder

    fun markPaymentPending(orderId: Long)

    fun markPaymentReady(orderId: Long)

    fun markPaid(orderId: Long)

    fun cancel(orderId: Long)
}
