package zoonza.commerce.order.application.port.`in`

import zoonza.commerce.order.application.dto.OrderDetailView
import zoonza.commerce.order.application.dto.OrderSummaryView
import zoonza.commerce.order.application.dto.PlaceCartOrderCommand
import zoonza.commerce.order.application.dto.PlaceDirectOrderCommand
import zoonza.commerce.order.application.dto.PlaceOrderResult

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
