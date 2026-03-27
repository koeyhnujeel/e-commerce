package zoonza.commerce.payment.adapter.out.persistence

import jakarta.persistence.*
import zoonza.commerce.payment.domain.Payment
import zoonza.commerce.payment.domain.PaymentMethod
import zoonza.commerce.payment.domain.PaymentStatus
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

@Entity
@Table(
    name = "payment",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_payment_payment_key",
            columnNames = ["payment_key"],
        ),
        UniqueConstraint(
            name = "uk_payment_order_active",
            columnNames = ["order_id", "active_marker"],
        ),
    ],
)
class PaymentJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "order_id", nullable = false)
    val orderId: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long = 0,

    @Column(name = "order_number", nullable = false, length = 64)
    val orderNumber: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: PaymentStatus = PaymentStatus.READY,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    var paymentMethod: PaymentMethod = PaymentMethod.CARD,

    @Column(name = "payment_key", length = 200)
    var paymentKey: String? = null,

    @Column(name = "provider_reference", nullable = false, length = 100)
    var providerReference: String = "",

    @Column(name = "failure_reason", length = 500)
    var failureReason: String? = null,

    @Column(name = "approved_at")
    var approvedAt: LocalDateTime? = null,

    @Column(name = "canceled_at")
    var canceledAt: LocalDateTime? = null,

    @Column(name = "active_marker", length = 16)
    var activeMarker: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.MIN,

    @Embedded
    @AttributeOverride(
        name = "amount",
        column = Column(name = "amount", nullable = false),
    )
    val amount: Money = Money(0),
) {
    fun isActive(): Boolean {
        return activeMarker != null
    }

    fun toDomain(): Payment {
        return Payment(
            id = id,
            orderId = orderId,
            memberId = memberId,
            orderNumber = orderNumber,
            status = status,
            paymentMethod = paymentMethod,
            paymentKey = paymentKey,
            providerReference = providerReference,
            failureReason = failureReason,
            approvedAt = approvedAt,
            canceledAt = canceledAt,
            activeMarker = activeMarker,
            createdAt = createdAt,
            amount = amount,
        )
    }

    companion object {
        fun from(payment: Payment): PaymentJpaEntity {
            return PaymentJpaEntity(
                id = payment.id,
                orderId = payment.orderId,
                memberId = payment.memberId,
                orderNumber = payment.orderNumber,
                status = payment.status,
                paymentMethod = payment.paymentMethod,
                paymentKey = payment.paymentKey,
                providerReference = payment.providerReference,
                failureReason = payment.failureReason,
                approvedAt = payment.approvedAt,
                canceledAt = payment.canceledAt,
                activeMarker = payment.activeMarker,
                createdAt = payment.createdAt,
                amount = payment.amount,
            )
        }
    }
}
