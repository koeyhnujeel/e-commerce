package zoonza.commerce.order.adapter.out.persistence

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.order.domain.OrderStatus
import java.time.LocalDateTime

interface OrderJpaRepository : JpaRepository<OrderJpaEntity, Long> {
    @EntityGraph(attributePaths = ["items"])
    fun findByIdAndMemberId(
        id: Long,
        memberId: Long,
    ): OrderJpaEntity?

    @EntityGraph(attributePaths = ["items"])
    fun findAllByMemberIdOrderByOrderedAtDesc(memberId: Long): List<OrderJpaEntity>

    @EntityGraph(attributePaths = ["items"])
    fun findAllByStatusAndExpiresAtBeforeOrderByExpiresAtAsc(
        status: OrderStatus,
        expiresAt: LocalDateTime,
        pageable: Pageable,
    ): List<OrderJpaEntity>
}
