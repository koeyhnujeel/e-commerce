package zoonza.commerce.payment.adapter.`in`.response

import zoonza.commerce.payment.application.dto.PreparePaymentResult

data class PreparePaymentResponse(
    val paymentId: Long,
    val provider: String,
    val clientKey: String,
    val providerOrderId: String,
    val orderName: String,
    val amount: Long,
    val successUrl: String,
    val failUrl: String,
) {
    companion object {
        fun from(result: PreparePaymentResult): PreparePaymentResponse {
            return PreparePaymentResponse(
                paymentId = result.paymentId,
                provider = result.provider.name,
                clientKey = result.clientKey,
                providerOrderId = result.providerOrderId,
                orderName = result.orderName,
                amount = result.amount,
                successUrl = result.successUrl,
                failUrl = result.failUrl,
            )
        }
    }
}
