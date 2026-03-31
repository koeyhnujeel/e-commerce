package zoonza.commerce.order.adapter.out.persistence

import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.order.application.port.out.OrderRepository
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderStatus
import java.time.LocalDateTime

@Repository
class OrderRepositoryAdapter(
    private val orderJpaRepository: OrderJpaRepository,
) : OrderRepository {
    override fun save(order: Order): Order {
        val entity =
            if (order.id == 0L) {
                OrderJpaEntity.from(order)
            } else {
                orderJpaRepository.findByIdOrNull(order.id)
                    ?.also { existing -> existing.updateFrom(order) }
                    ?: OrderJpaEntity.from(order)
            }

        return orderJpaRepository.save(entity).toDomain()
    }

    override fun findById(id: Long): Order? {
        return orderJpaRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun findByIdAndMemberId(
        id: Long,
        memberId: Long,
    ): Order? {
        return orderJpaRepository.findByIdAndMemberId(id, memberId)?.toDomain()
    }

    override fun findAllByMemberId(memberId: Long): List<Order> {
        return orderJpaRepository.findAllByMemberIdOrderByOrderedAtDesc(memberId).map(OrderJpaEntity::toDomain)
    }

    override fun findExpiredPendingOrders(
        now: LocalDateTime,
        limit: Int,
    ): List<Order> {
        return orderJpaRepository.findAllByStatusAndExpiresAtBeforeOrderByExpiresAtAsc(
            status = OrderStatus.PENDING_PAYMENT,
            expiresAt = now,
            pageable = PageRequest.of(0, limit),
        ).map(OrderJpaEntity::toDomain)
    }
}
