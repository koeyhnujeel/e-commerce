package zoonza.commerce.payment.domain

import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class PaymentRefund(
    val id: Long = 0L,
    val refundIdempotencyKey: String,
    val reason: String,
    val amount: Money,
    val refundedAt: LocalDateTime,
    val providerTransactionKey: String? = null,
) {
    companion object {
        fun create(
            refundIdempotencyKey: String,
            reason: String,
            amount: Money,
            refundedAt: LocalDateTime,
            providerTransactionKey: String?,
        ): PaymentRefund {
            require(refundIdempotencyKey.isNotBlank()) { "환불 멱등키는 비어 있을 수 없습니다." }
            require(reason.isNotBlank()) { "환불 사유는 비어 있을 수 없습니다." }

            return PaymentRefund(
                refundIdempotencyKey = refundIdempotencyKey.trim(),
                reason = reason.trim(),
                amount = amount,
                refundedAt = refundedAt,
                providerTransactionKey = providerTransactionKey?.trim()?.takeIf { it.isNotBlank() },
            )
        }
    }
}
