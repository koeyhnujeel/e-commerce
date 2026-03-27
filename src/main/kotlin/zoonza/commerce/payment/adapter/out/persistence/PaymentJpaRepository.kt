package zoonza.commerce.payment.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface PaymentJpaRepository : JpaRepository<PaymentJpaEntity, Long> {
    fun existsByOrderIdAndActiveMarkerIsNotNull(orderId: Long): Boolean

    fun findByIdAndMemberId(
        id: Long,
        memberId: Long,
    ): PaymentJpaEntity?
}
