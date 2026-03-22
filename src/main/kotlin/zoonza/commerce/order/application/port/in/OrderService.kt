package zoonza.commerce.order.application.port.`in`

interface OrderService {
    fun confirmPurchase(
        memberId: Long,
        orderItemId: Long,
    )
}
