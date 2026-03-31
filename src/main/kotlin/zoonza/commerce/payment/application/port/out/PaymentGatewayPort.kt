package zoonza.commerce.payment.application.port.out

import java.time.LocalDateTime

interface PaymentGatewayPort {
    fun confirm(
        paymentKey: String,
        providerOrderId: String,
        amount: Long,
        idempotencyKey: String,
    ): PaymentGatewayConfirmation

    fun cancel(
        paymentKey: String,
        cancelReason: String,
        amount: Long,
        idempotencyKey: String,
    ): PaymentGatewayCancellation

    fun get(paymentKey: String): PaymentGatewayLookup
}

data class PaymentGatewayConfirmation(
    val paymentKey: String,
    val providerOrderId: String,
    val totalAmount: Long,
    val method: String?,
    val approvedAt: LocalDateTime,
)

data class PaymentGatewayCancellation(
    val paymentKey: String,
    val totalAmount: Long,
    val canceledAt: LocalDateTime,
    val transactionKey: String?,
)

data class PaymentGatewayLookup(
    val paymentKey: String,
    val providerOrderId: String,
    val status: String,
    val totalAmount: Long,
    val method: String?,
    val approvedAt: LocalDateTime?,
)
