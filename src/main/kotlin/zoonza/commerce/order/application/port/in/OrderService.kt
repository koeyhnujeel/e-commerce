package zoonza.commerce.order.application.port.`in`

import zoonza.commerce.order.application.dto.*

interface OrderService {
    fun placeDirectOrder(
        memberId: Long,
        command: PlaceDirectOrderCommand,
    ): PlaceOrderResult

    fun placeCartOrder(
        memberId: Long,
        command: PlaceCartOrderCommand,
    ): PlaceOrderResult

    fun getMyOrders(memberId: Long): List<OrderSummaryView>

    fun getMyOrder(
        memberId: Long,
        orderId: Long,
    ): OrderDetailView

    fun cancel(
        memberId: Long,
        orderId: Long,
    )
}
