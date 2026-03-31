package zoonza.commerce.payment.adapter.out.persistence

import jakarta.persistence.*
import zoonza.commerce.payment.domain.Payment
import zoonza.commerce.payment.domain.PaymentCloseReason
import zoonza.commerce.payment.domain.PaymentProvider
import zoonza.commerce.payment.domain.PaymentStatus
import zoonza.commerce.shared.Money
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "payments",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_payments_order_id", columnNames = ["order_id"]),
    ],
)
class PaymentJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "order_id", nullable = false)
    var orderId: Long = 0L,

    @Column(name = "order_number", nullable = false)
    var orderNumber: String = "",

    @Column(name = "member_id", nullable = false)
    var memberId: Long = 0L,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    var provider: PaymentProvider = PaymentProvider.TOSS_PAYMENTS,

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 0)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: PaymentStatus = PaymentStatus.READY,

    @Column(name = "approved_at")
    var approvedAt: LocalDateTime? = null,

    @Column(name = "refunded_at")
    var refundedAt: LocalDateTime? = null,

    @Column(name = "closed_at")
    var closedAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "close_reason", length = 50)
    var closeReason: PaymentCloseReason? = null,

    @OneToMany(mappedBy = "payment", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("attemptNumber ASC")
    val attempts: MutableList<PaymentAttemptJpaEntity> = mutableListOf(),

    @OneToMany(mappedBy = "payment", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("id ASC")
    val refunds: MutableList<PaymentRefundJpaEntity> = mutableListOf(),
) {
    companion object {
        fun from(payment: Payment): PaymentJpaEntity {
            val entity =
                PaymentJpaEntity(
                    id = payment.id,
                    orderId = payment.orderId,
                    orderNumber = payment.orderNumber,
                    memberId = payment.memberId,
                    provider = payment.provider,
                    totalAmount = payment.totalAmount.amount,
                    status = payment.status,
                    approvedAt = payment.approvedAt,
                    refundedAt = payment.refundedAt,
                    closedAt = payment.closedAt,
                    closeReason = payment.closeReason,
                )
            entity.replaceAttempts(payment)
            entity.replaceRefunds(payment)
            return entity
        }
    }

    fun toDomain(): Payment {
        return Payment(
            id = id,
            orderId = orderId,
            orderNumber = orderNumber,
            memberId = memberId,
            provider = provider,
            totalAmount = Money(totalAmount),
            status = status,
            approvedAt = approvedAt,
            refundedAt = refundedAt,
            closedAt = closedAt,
            closeReason = closeReason,
            attempts = attempts.map(PaymentAttemptJpaEntity::toDomain).toMutableList(),
            refunds = refunds.map(PaymentRefundJpaEntity::toDomain).toMutableList(),
        )
    }

    fun updateFrom(payment: Payment) {
        orderId = payment.orderId
        orderNumber = payment.orderNumber
        memberId = payment.memberId
        provider = payment.provider
        totalAmount = payment.totalAmount.amount
        status = payment.status
        approvedAt = payment.approvedAt
        refundedAt = payment.refundedAt
        closedAt = payment.closedAt
        closeReason = payment.closeReason
        replaceAttempts(payment)
        replaceRefunds(payment)
    }

    private fun replaceAttempts(payment: Payment) {
        attempts.clear()
        attempts.addAll(payment.attempts.map { PaymentAttemptJpaEntity.from(it, this) })
    }

    private fun replaceRefunds(payment: Payment) {
        refunds.clear()
        refunds.addAll(payment.refunds.map { PaymentRefundJpaEntity.from(it, this) })
    }
}
