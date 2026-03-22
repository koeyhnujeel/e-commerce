package zoonza.commerce.payment.application.port.out

import zoonza.commerce.payment.domain.Payment

interface PaymentRepository {
    fun existsActiveByOrderId(orderId: Long): Boolean

    fun findById(paymentId: Long): Payment?

    fun findByIdAndMemberId(
        paymentId: Long,
        memberId: Long,
    ): Payment?

    fun save(payment: Payment): Payment
}
