package zoonza.commerce.payment.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import zoonza.commerce.payment.domain.PaymentAttempt
import zoonza.commerce.payment.domain.PaymentAttemptStatus
import zoonza.commerce.payment.domain.PaymentMethod
import java.time.LocalDateTime

@Entity
@Table(
    name = "payment_attempts",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_payment_attempts_provider_order_id", columnNames = ["provider_order_id"]),
        UniqueConstraint(name = "uk_payment_attempts_callback_token", columnNames = ["callback_token"]),
    ],
)
class PaymentAttemptJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    var payment: PaymentJpaEntity? = null,

    @Column(name = "attempt_number", nullable = false)
    var attemptNumber: Int = 0,

    @Column(name = "provider_order_id", nullable = false)
    var providerOrderId: String = "",

    @Column(name = "callback_token", nullable = false)
    var callbackToken: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: PaymentAttemptStatus = PaymentAttemptStatus.PREPARED,

    @Column(name = "prepared_at", nullable = false)
    var preparedAt: LocalDateTime = LocalDateTime.MIN,

    @Column(name = "approved_at")
    var approvedAt: LocalDateTime? = null,

    @Column(name = "failed_at")
    var failedAt: LocalDateTime? = null,

    @Column(name = "payment_key")
    var paymentKey: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 50)
    var method: PaymentMethod? = null,

    @Column(name = "failure_code")
    var failureCode: String? = null,

    @Column(name = "failure_message")
    var failureMessage: String? = null,
) {
    companion object {
        fun from(
            attempt: PaymentAttempt,
            payment: PaymentJpaEntity,
        ): PaymentAttemptJpaEntity {
            return PaymentAttemptJpaEntity(
                id = attempt.id,
                payment = payment,
                attemptNumber = attempt.attemptNumber,
                providerOrderId = attempt.providerOrderId,
                callbackToken = attempt.callbackToken,
                status = attempt.status,
                preparedAt = attempt.preparedAt,
                approvedAt = attempt.approvedAt,
                failedAt = attempt.failedAt,
                paymentKey = attempt.paymentKey,
                method = attempt.method,
                failureCode = attempt.failureCode,
                failureMessage = attempt.failureMessage,
            )
        }
    }

    fun toDomain(): PaymentAttempt {
        return PaymentAttempt(
            id = id,
            attemptNumber = attemptNumber,
            providerOrderId = providerOrderId,
            callbackToken = callbackToken,
            status = status,
            preparedAt = preparedAt,
            approvedAt = approvedAt,
            failedAt = failedAt,
            paymentKey = paymentKey,
            method = method,
            failureCode = failureCode,
            failureMessage = failureMessage,
        )
    }
}
