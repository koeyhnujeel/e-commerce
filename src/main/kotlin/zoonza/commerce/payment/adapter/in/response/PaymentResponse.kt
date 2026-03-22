package zoonza.commerce.payment.adapter.`in`.response

import zoonza.commerce.payment.application.dto.PaymentDetail
import zoonza.commerce.payment.application.dto.PaymentCheckout
import zoonza.commerce.payment.domain.PaymentMethod
import zoonza.commerce.payment.domain.PaymentStatus
import java.time.LocalDateTime

data class CreatePaymentResponse(
    val paymentId: Long,
    val orderId: Long,
    val orderNumber: String,
    val status: PaymentStatus,
    val amount: Long,
    val checkout: PaymentCheckoutResponse,
    val createdAt: LocalDateTime,
)

data class PaymentCheckoutResponse(
    val clientKey: String,
    val orderId: String,
    val orderName: String,
    val customerKey: String,
    val amount: Long,
    val successUrl: String,
    val failUrl: String,
)

data class PaymentDetailResponse(
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

fun PaymentCheckout.toResponse(): PaymentCheckoutResponse {
    return PaymentCheckoutResponse(
        clientKey = clientKey,
        orderId = orderId,
        orderName = orderName,
        customerKey = customerKey,
        amount = amount,
        successUrl = successUrl,
        failUrl = failUrl,
    )
}

fun PaymentDetail.toResponse(): PaymentDetailResponse {
    return PaymentDetailResponse(
        paymentId = paymentId,
        orderId = orderId,
        orderNumber = orderNumber,
        status = status,
        paymentMethod = paymentMethod,
        amount = amount,
        paymentKey = paymentKey,
        providerReference = providerReference,
        failureReason = failureReason,
        createdAt = createdAt,
        approvedAt = approvedAt,
        canceledAt = canceledAt,
    )
}
