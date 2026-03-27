package zoonza.commerce.order.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<OrderJpaEntity, Long>, OrderQueryRepository {
    fun findAllByMemberIdAndDeletedAtIsNullOrderByOrderedAtDescIdDesc(memberId: Long): List<OrderJpaEntity>
}
