package zoonza.commerce.payment.application.dto

import zoonza.commerce.payment.domain.PaymentProvider

data class PreparePaymentResult(
    val paymentId: Long,
    val provider: PaymentProvider,
    val clientKey: String,
    val providerOrderId: String,
    val orderName: String,
    val amount: Long,
    val successUrl: String,
    val failUrl: String,
)
