package zoonza.commerce.order.application.port.out

import zoonza.commerce.order.domain.Order
import java.time.LocalDateTime

interface OrderRepository {
    fun save(order: Order): Order

    fun findById(id: Long): Order?

    fun findByIdAndMemberId(
        id: Long,
        memberId: Long,
    ): Order?

    fun findAllByMemberId(memberId: Long): List<Order>

    fun findExpiredPendingOrders(
        now: LocalDateTime,
        limit: Int,
    ): List<Order>
}
