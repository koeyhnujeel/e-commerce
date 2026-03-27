package zoonza.commerce.order.adapter.out.persistence

import zoonza.commerce.order.ReviewablePurchase
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
    ): OrderJpaEntity?

    fun findOrderDetailByIdAndMemberId(
        orderId: Long,
        memberId: Long,
    ): OrderJpaEntity?

    fun findOrderDetailById(orderId: Long): OrderJpaEntity?
}
