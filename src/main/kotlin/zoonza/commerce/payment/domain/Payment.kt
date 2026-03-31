package zoonza.commerce.payment.domain

import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class Payment(
    val id: Long = 0L,
    val orderId: Long,
    val orderNumber: String,
    val memberId: Long,
    val provider: PaymentProvider,
    val totalAmount: Money,
    var status: PaymentStatus,
    var approvedAt: LocalDateTime? = null,
    var refundedAt: LocalDateTime? = null,
    var closedAt: LocalDateTime? = null,
    var closeReason: PaymentCloseReason? = null,
    val attempts: MutableList<PaymentAttempt> = mutableListOf(),
    val refunds: MutableList<PaymentRefund> = mutableListOf(),
) {
    companion object {
        fun create(
            orderId: Long,
            orderNumber: String,
            memberId: Long,
            provider: PaymentProvider,
            totalAmount: Money,
        ): Payment {
            require(orderId > 0) { "주문 ID는 1 이상이어야 합니다." }
            require(orderNumber.isNotBlank()) { "주문번호는 비어 있을 수 없습니다." }
            require(memberId > 0) { "회원 ID는 1 이상이어야 합니다." }

            return Payment(
                orderId = orderId,
                orderNumber = orderNumber.trim(),
                memberId = memberId,
                provider = provider,
                totalAmount = totalAmount,
                status = PaymentStatus.READY,
            )
        }
    }

    fun prepareAttempt(
        providerOrderId: String,
        callbackToken: String,
        preparedAt: LocalDateTime,
    ): PaymentAttempt {
        require(status == PaymentStatus.READY) { "준비 가능한 결제가 아닙니다." }

        val attempt =
            PaymentAttempt.create(
                attemptNumber = attempts.size + 1,
                providerOrderId = providerOrderId,
                callbackToken = callbackToken,
                preparedAt = preparedAt,
            )
        attempts.add(attempt)
        return attempt
    }

    fun approveAttempt(
        callbackToken: String,
        paymentKey: String,
        method: PaymentMethod,
        approvedAt: LocalDateTime,
    ): PaymentAttempt {
        require(status == PaymentStatus.READY) { "승인 가능한 결제가 아닙니다." }

        val attempt = findAttemptByCallbackToken(callbackToken)
        attempt.approve(paymentKey = paymentKey, method = method, approvedAt = approvedAt)
        status = PaymentStatus.APPROVED
        this.approvedAt = approvedAt
        return attempt
    }

    fun failAttempt(
        callbackToken: String,
        failureCode: String,
        failureMessage: String,
        failedAt: LocalDateTime,
    ): PaymentAttempt {
        require(status == PaymentStatus.READY) { "실패 처리 가능한 결제가 아닙니다." }

        val attempt = findAttemptByCallbackToken(callbackToken)
        attempt.fail(failureCode = failureCode, failureMessage = failureMessage, failedAt = failedAt)
        return attempt
    }

    fun close(
        reason: PaymentCloseReason,
        closedAt: LocalDateTime,
    ) {
        require(status == PaymentStatus.READY) { "닫을 수 없는 결제입니다." }

        status = PaymentStatus.CLOSED
        this.closeReason = reason
        this.closedAt = closedAt
    }

    fun refund(
        refundIdempotencyKey: String,
        reason: String,
        refundedAt: LocalDateTime,
        providerTransactionKey: String?,
    ): PaymentRefund {
        require(status == PaymentStatus.APPROVED) { "환불 가능한 결제가 아닙니다." }
        require(refunds.isEmpty()) { "이미 환불된 결제입니다." }

        val refund =
            PaymentRefund.create(
                refundIdempotencyKey = refundIdempotencyKey,
                reason = reason,
                amount = totalAmount,
                refundedAt = refundedAt,
                providerTransactionKey = providerTransactionKey,
            )

        refunds.add(refund)
        status = PaymentStatus.REFUNDED
        this.refundedAt = refundedAt
        return refund
    }

    fun findAttemptByCallbackToken(callbackToken: String): PaymentAttempt {
        return attempts.first { it.callbackToken == callbackToken }
    }

    fun findAttemptByProviderOrderId(providerOrderId: String): PaymentAttempt {
        return attempts.first { it.providerOrderId == providerOrderId }
    }

    fun approvedPaymentKey(): String {
        return attempts.first { it.status == PaymentAttemptStatus.APPROVED }.paymentKey
            ?: throw IllegalStateException("승인된 결제 시도의 paymentKey가 없습니다.")
    }
}
