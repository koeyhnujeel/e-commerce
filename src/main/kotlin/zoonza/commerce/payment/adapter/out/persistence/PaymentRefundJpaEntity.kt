package zoonza.commerce.payment.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import zoonza.commerce.payment.domain.PaymentRefund
import zoonza.commerce.shared.Money
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "payment_refunds",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_payment_refunds_idempotency_key", columnNames = ["refund_idempotency_key"]),
    ],
)
class PaymentRefundJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    var payment: PaymentJpaEntity? = null,

    @Column(name = "refund_idempotency_key", nullable = false)
    var refundIdempotencyKey: String = "",

    @Column(name = "cancel_reason", nullable = false)
    var reason: String = "",

    @Column(name = "amount", nullable = false, precision = 19, scale = 0)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "refunded_at", nullable = false)
    var refundedAt: LocalDateTime = LocalDateTime.MIN,

    @Column(name = "provider_transaction_key")
    var providerTransactionKey: String? = null,
) {
    companion object {
        fun from(
            refund: PaymentRefund,
            payment: PaymentJpaEntity,
        ): PaymentRefundJpaEntity {
            return PaymentRefundJpaEntity(
                id = refund.id,
                payment = payment,
                refundIdempotencyKey = refund.refundIdempotencyKey,
                reason = refund.reason,
                amount = refund.amount.amount,
                refundedAt = refund.refundedAt,
                providerTransactionKey = refund.providerTransactionKey,
            )
        }
    }

    fun toDomain(): PaymentRefund {
        return PaymentRefund(
            id = id,
            refundIdempotencyKey = refundIdempotencyKey,
            reason = reason,
            amount = Money(amount),
            refundedAt = refundedAt,
            providerTransactionKey = providerTransactionKey,
        )
    }
}
