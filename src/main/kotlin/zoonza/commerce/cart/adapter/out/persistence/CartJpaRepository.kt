package zoonza.commerce.cart.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface CartJpaRepository : JpaRepository<CartJpaEntity, Long> {
    fun findByMemberId(memberId: Long): CartJpaEntity?
}
