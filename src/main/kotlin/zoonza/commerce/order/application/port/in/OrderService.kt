package zoonza.commerce.order.application.port.`in`

import zoonza.commerce.order.application.dto.*

interface OrderService {
    fun createOrder(
        memberId: Long,
        command: CreateOrderCommand,
    ): CreateOrderResult

    fun getOrders(memberId: Long): List<OrderSummary>

    fun getOrder(
        memberId: Long,
        orderId: Long,
    ): OrderDetail

    fun updateOrder(
        memberId: Long,
        orderId: Long,
        command: UpdateOrderCommand,
    ): OrderDetail

    fun deleteOrder(
        memberId: Long,
        orderId: Long,
    )

    fun confirmPurchase(
        memberId: Long,
        orderItemId: Long,
    )
}
