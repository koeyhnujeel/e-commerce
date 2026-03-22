package zoonza.commerce.payment.domain

import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
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
class Payment private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "order_id", nullable = false)
    val orderId: Long,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "order_number", nullable = false, length = 64)
    val orderNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    var status: PaymentStatus,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    var paymentMethod: PaymentMethod,

    @Column(name = "payment_key", length = 200)
    var paymentKey: String? = null,

    @Column(name = "provider_reference", nullable = false, length = 100)
    var providerReference: String,

    @Column(name = "failure_reason", length = 500)
    var failureReason: String? = null,

    @Column(name = "approved_at")
    var approvedAt: LocalDateTime? = null,

    @Column(name = "canceled_at")
    var canceledAt: LocalDateTime? = null,

    @Column(name = "active_marker", length = 16)
    var activeMarker: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,

    @Embedded
    @AttributeOverride(
        name = "amount",
        column = Column(name = "amount", nullable = false),
    )
    val amount: Money,
) {
    companion object {
        private const val ACTIVE_MARKER = "ACTIVE"

        fun create(
            orderId: Long,
            memberId: Long,
            orderNumber: String,
            amount: Money,
            paymentMethod: PaymentMethod,
            providerReference: String,
            createdAt: LocalDateTime,
            id: Long = 0,
        ): Payment {
            require(id >= 0) { "결제 ID는 0 이상이어야 합니다." }
            require(orderId > 0) { "주문 ID는 1 이상이어야 합니다." }
            require(memberId > 0) { "회원 ID는 1 이상이어야 합니다." }
            require(orderNumber.isNotBlank()) { "주문 번호는 비어 있을 수 없습니다." }
            require(providerReference.isNotBlank()) { "결제 참조값은 비어 있을 수 없습니다." }

            return Payment(
                id = id,
                orderId = orderId,
                memberId = memberId,
                orderNumber = orderNumber.trim(),
                status = PaymentStatus.READY,
                paymentMethod = paymentMethod,
                providerReference = providerReference.trim(),
                activeMarker = ACTIVE_MARKER,
                createdAt = createdAt,
                amount = amount,
            )
        }
    }

    fun isActive(): Boolean {
        return activeMarker != null
    }

    fun canConfirm(): Boolean {
        return status == PaymentStatus.READY
    }

    fun canCancel(): Boolean {
        return status == PaymentStatus.CONFIRMED
    }

    fun confirm(
        paymentKey: String,
        paymentMethod: PaymentMethod,
        providerReference: String,
        approvedAt: LocalDateTime,
    ) {
        require(canConfirm()) { "확정할 수 없는 결제입니다." }
        require(paymentKey.isNotBlank()) { "결제 키는 비어 있을 수 없습니다." }
        require(providerReference.isNotBlank()) { "결제 참조값은 비어 있을 수 없습니다." }

        this.status = PaymentStatus.CONFIRMED
        this.paymentKey = paymentKey.trim()
        this.paymentMethod = paymentMethod
        this.providerReference = providerReference.trim()
        this.failureReason = null
        this.approvedAt = approvedAt
        this.activeMarker = ACTIVE_MARKER
    }

    fun fail(reason: String) {
        require(canConfirm()) { "실패 처리할 수 없는 결제입니다." }
        require(reason.isNotBlank()) { "실패 사유는 비어 있을 수 없습니다." }

        this.status = PaymentStatus.FAILED
        this.failureReason = reason.trim()
        this.activeMarker = null
    }

    fun cancel(
        reason: String?,
        canceledAt: LocalDateTime,
    ) {
        require(canCancel()) { "취소할 수 없는 결제입니다." }

        this.status = PaymentStatus.CANCELED
        this.failureReason = reason?.trim()?.takeIf(String::isNotBlank)
        this.canceledAt = canceledAt
        this.activeMarker = null
    }
}
