package zoonza.commerce.order.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.order.domain.Order

interface OrderJpaRepository : JpaRepository<Order, Long>, OrderQueryRepository {
    fun findAllByMemberIdAndDeletedAtIsNullOrderByOrderedAtDescIdDesc(memberId: Long): List<Order>
}
