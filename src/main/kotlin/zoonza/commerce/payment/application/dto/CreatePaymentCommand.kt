package zoonza.commerce.payment.application.dto

import zoonza.commerce.payment.domain.PaymentMethod
import zoonza.commerce.payment.domain.PaymentStatus
import java.time.LocalDateTime

data class CreatePaymentCommand(
    val amount: Long,
    val paymentMethod: PaymentMethod,
)

data class CreatePaymentResult(
    val paymentId: Long,
    val orderId: Long,
    val orderNumber: String,
    val status: PaymentStatus,
    val amount: Long,
    val checkout: PaymentCheckout,
    val createdAt: LocalDateTime,
)

data class PaymentCheckout(
    val clientKey: String,
    val orderId: String,
    val orderName: String,
    val customerKey: String,
    val amount: Long,
    val successUrl: String,
    val failUrl: String,
)
