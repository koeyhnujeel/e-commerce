package zoonza.commerce.payment.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.payment.domain.Payment

interface PaymentJpaRepository : JpaRepository<Payment, Long> {
    fun existsByOrderIdAndActiveMarkerIsNotNull(orderId: Long): Boolean

    fun findByIdAndMemberId(
        id: Long,
        memberId: Long,
    ): Payment?
}
