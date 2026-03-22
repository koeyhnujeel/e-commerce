package zoonza.commerce.payment.application.port.`in`

import zoonza.commerce.payment.application.dto.CreatePaymentCommand
import zoonza.commerce.payment.application.dto.CreatePaymentResult
import zoonza.commerce.payment.application.dto.CancelPaymentCommand
import zoonza.commerce.payment.application.dto.ConfirmPaymentCommand
import zoonza.commerce.payment.application.dto.PaymentDetail

interface PaymentService {
    fun createPayment(
        memberId: Long,
        orderId: Long,
        command: CreatePaymentCommand,
    ): CreatePaymentResult

    fun getPayment(
        memberId: Long,
        paymentId: Long,
    ): PaymentDetail

    fun confirmPayment(
        memberId: Long,
        paymentId: Long,
        command: ConfirmPaymentCommand,
    ): PaymentDetail

    fun cancelPayment(
        memberId: Long,
        paymentId: Long,
        command: CancelPaymentCommand,
    ): PaymentDetail
}
