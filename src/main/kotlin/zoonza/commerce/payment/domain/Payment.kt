package zoonza.commerce.payment.domain

import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class Payment(
    val id: Long = 0,
    val orderId: Long,
    val memberId: Long,
    val orderNumber: String,
    var status: PaymentStatus,
    var paymentMethod: PaymentMethod,
    var paymentKey: String? = null,
    var providerReference: String,
    var failureReason: String? = null,
    var approvedAt: LocalDateTime? = null,
    var canceledAt: LocalDateTime? = null,
    var activeMarker: String?,
    val createdAt: LocalDateTime,
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
        providerReference: String?,
        canceledAt: LocalDateTime,
    ) {
        require(canCancel()) { "취소할 수 없는 결제입니다." }

        this.status = PaymentStatus.CANCELED
        this.providerReference = providerReference?.trim()?.takeIf(String::isNotBlank) ?: this.providerReference
        this.failureReason = reason?.trim()?.takeIf(String::isNotBlank)
        this.canceledAt = canceledAt
        this.activeMarker = null
    }
}
