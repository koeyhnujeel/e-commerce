package zoonza.commerce.order.adapter.out.persistence

import zoonza.commerce.order.ReviewablePurchase
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderItemStatus

interface OrderQueryRepository {
    fun findReviewablePurchases(
        memberId: Long,
        productId: Long,
        status: OrderItemStatus,
    ): List<ReviewablePurchase>

    fun findOrderByMemberIdAndOrderItemId(
        memberId: Long,
        orderItemId: Long,
    ): Order?

    fun findOrderDetailByIdAndMemberId(
        orderId: Long,
        memberId: Long,
    ): Order?

    fun findOrderDetailById(orderId: Long): Order?
}
