package zoonza.commerce.payment.adapter.out.persistence

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.payment.application.port.out.PaymentRepository
import zoonza.commerce.payment.domain.Payment

@Repository
class PaymentRepositoryAdapter(
    private val paymentJpaRepository: PaymentJpaRepository,
) : PaymentRepository {
    override fun save(payment: Payment): Payment {
        val entity =
            if (payment.id == 0L) {
                PaymentJpaEntity.from(payment)
            } else {
                paymentJpaRepository.findByIdOrNull(payment.id)
                    ?.also { it.updateFrom(payment) }
                    ?: PaymentJpaEntity.from(payment)
            }

        return paymentJpaRepository.save(entity).toDomain()
    }

    override fun findByOrderId(orderId: Long): Payment? {
        return paymentJpaRepository.findByOrderId(orderId)?.toDomain()
    }

    override fun findByProviderOrderId(providerOrderId: String): Payment? {
        return paymentJpaRepository.findDistinctByAttemptsProviderOrderId(providerOrderId)?.toDomain()
    }

    override fun findByCallbackToken(callbackToken: String): Payment? {
        return paymentJpaRepository.findDistinctByAttemptsCallbackToken(callbackToken)?.toDomain()
    }
}
