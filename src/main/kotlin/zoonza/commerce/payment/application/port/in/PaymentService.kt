package zoonza.commerce.payment.application.port.`in`

import zoonza.commerce.payment.application.dto.PaymentRedirectResult
import zoonza.commerce.payment.application.dto.PreparePaymentResult

interface PaymentService {
    fun prepare(
        memberId: Long,
        orderId: Long,
    ): PreparePaymentResult

    fun handleSuccessCallback(
        callbackToken: String,
        providerOrderId: String,
        paymentKey: String,
        amount: Long,
    ): PaymentRedirectResult

    fun handleFailCallback(
        callbackToken: String,
        code: String,
        message: String,
    ): PaymentRedirectResult

    fun handleWebhook(
        eventType: String,
        paymentKey: String,
        providerOrderId: String?,
    )

    fun refund(
        orderId: Long,
        reason: String,
    )
}
