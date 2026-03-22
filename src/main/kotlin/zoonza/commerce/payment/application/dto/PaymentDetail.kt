package zoonza.commerce.payment.application.dto

import zoonza.commerce.payment.domain.PaymentMethod
import zoonza.commerce.payment.domain.PaymentStatus
import java.time.LocalDateTime

data class PaymentDetail(
    val paymentId: Long,
    val orderId: Long,
    val orderNumber: String,
    val status: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val amount: Long,
    val paymentKey: String?,
    val providerReference: String,
    val failureReason: String?,
    val createdAt: LocalDateTime,
    val approvedAt: LocalDateTime?,
    val canceledAt: LocalDateTime?,
)
