package zoonza.commerce.order

interface OrderApi {
    fun findPendingPaymentTarget(orderId: Long): PendingPaymentOrder

    fun markPaid(orderId: Long)
}
