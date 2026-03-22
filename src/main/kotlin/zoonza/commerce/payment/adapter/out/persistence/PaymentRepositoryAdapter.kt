package zoonza.commerce.payment.adapter.out.persistence

import org.springframework.stereotype.Repository
import zoonza.commerce.payment.application.port.out.PaymentRepository
import zoonza.commerce.payment.domain.Payment

@Repository
class PaymentRepositoryAdapter(
    private val paymentJpaRepository: PaymentJpaRepository,
) : PaymentRepository {
    override fun existsActiveByOrderId(orderId: Long): Boolean {
        return paymentJpaRepository.existsByOrderIdAndActiveMarkerIsNotNull(orderId)
    }

    override fun findById(paymentId: Long): Payment? {
        return paymentJpaRepository.findById(paymentId).orElse(null)
    }

    override fun findByIdAndMemberId(
        paymentId: Long,
        memberId: Long,
    ): Payment? {
        return paymentJpaRepository.findByIdAndMemberId(paymentId, memberId)
    }

    override fun save(payment: Payment): Payment {
        return paymentJpaRepository.save(payment)
    }
}
