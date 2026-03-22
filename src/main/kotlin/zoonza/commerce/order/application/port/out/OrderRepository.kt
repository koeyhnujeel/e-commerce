package zoonza.commerce.order.application.port.out

import zoonza.commerce.order.ReviewablePurchase
import zoonza.commerce.order.domain.Order

interface OrderRepository {
    fun findReviewablePurchase(
        memberId: Long,
        productId: Long,
    ): List<ReviewablePurchase>

    fun findOrders(memberId: Long): List<Order>

    fun findOrderByIdAndMemberId(
        orderId: Long,
        memberId: Long,
    ): Order?

    fun findOrderByMemberIdAndOrderItemId(
        memberId: Long,
        orderItemId: Long,
    ): Order?

    fun findOrderById(orderId: Long): Order?

    fun save(order: Order): Order
}
