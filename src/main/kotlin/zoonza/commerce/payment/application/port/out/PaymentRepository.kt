package zoonza.commerce.payment.application.port.out

import zoonza.commerce.payment.domain.Payment

interface PaymentRepository {
    fun save(payment: Payment): Payment

    fun findByOrderId(orderId: Long): Payment?

    fun findByProviderOrderId(providerOrderId: String): Payment?

    fun findByCallbackToken(callbackToken: String): Payment?
}
