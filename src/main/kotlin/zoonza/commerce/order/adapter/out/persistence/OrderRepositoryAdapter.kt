package zoonza.commerce.order.adapter.out.persistence

import org.springframework.stereotype.Repository
import zoonza.commerce.order.ReviewablePurchase
import zoonza.commerce.order.application.port.out.OrderRepository
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderItemStatus

@Repository
class OrderRepositoryAdapter(
    private val orderJpaRepository: OrderJpaRepository,
) : OrderRepository {
    override fun findReviewablePurchase(
        memberId: Long,
        productId: Long,
    ): List<ReviewablePurchase> {
        return orderJpaRepository.findReviewablePurchases(
            memberId = memberId,
            productId = productId,
            status = OrderItemStatus.PURCHASE_CONFIRMED,
        )
    }

    override fun findOrders(memberId: Long): List<Order> {
        return orderJpaRepository.findAllByMemberIdOrderByOrderedAtDescIdDesc(memberId)
    }

    override fun findOrderByIdAndMemberId(
        orderId: Long,
        memberId: Long,
    ): Order? {
        return orderJpaRepository.findOrderDetailByIdAndMemberId(orderId, memberId)
    }

    override fun findOrderByMemberIdAndOrderItemId(
        memberId: Long,
        orderItemId: Long,
    ): Order? {
        return orderJpaRepository.findOrderByMemberIdAndOrderItemId(memberId, orderItemId)
    }

    override fun save(order: Order): Order {
        return orderJpaRepository.save(order)
    }
}
