package zoonza.commerce.order.application.port.`in`

import zoonza.commerce.order.application.dto.CreateOrderCommand
import zoonza.commerce.order.application.dto.CreateOrderResult
import zoonza.commerce.order.application.dto.OrderDetail
import zoonza.commerce.order.application.dto.OrderSummary

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

    fun confirmPurchase(
        memberId: Long,
        orderItemId: Long,
    )
}
