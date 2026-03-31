package zoonza.commerce.payment.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface PaymentJpaRepository : JpaRepository<PaymentJpaEntity, Long> {
    fun findByOrderId(orderId: Long): PaymentJpaEntity?

    fun findDistinctByAttemptsProviderOrderId(providerOrderId: String): PaymentJpaEntity?

    fun findDistinctByAttemptsCallbackToken(callbackToken: String): PaymentJpaEntity?
}
