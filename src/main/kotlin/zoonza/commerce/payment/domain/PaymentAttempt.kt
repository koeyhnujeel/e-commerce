package zoonza.commerce.payment.domain

import java.time.LocalDateTime

class PaymentAttempt(
    val id: Long = 0L,
    val attemptNumber: Int,
    val providerOrderId: String,
    val callbackToken: String,
    var status: PaymentAttemptStatus,
    val preparedAt: LocalDateTime,
    var approvedAt: LocalDateTime? = null,
    var failedAt: LocalDateTime? = null,
    var paymentKey: String? = null,
    var method: PaymentMethod? = null,
    var failureCode: String? = null,
    var failureMessage: String? = null,
) {
    companion object {
        fun create(
            attemptNumber: Int,
            providerOrderId: String,
            callbackToken: String,
            preparedAt: LocalDateTime,
        ): PaymentAttempt {
            require(attemptNumber > 0) { "결제 시도 순번은 1 이상이어야 합니다." }
            require(providerOrderId.isNotBlank()) { "결제사 주문번호는 비어 있을 수 없습니다." }
            require(callbackToken.isNotBlank()) { "콜백 토큰은 비어 있을 수 없습니다." }

            return PaymentAttempt(
                attemptNumber = attemptNumber,
                providerOrderId = providerOrderId.trim(),
                callbackToken = callbackToken.trim(),
                status = PaymentAttemptStatus.PREPARED,
                preparedAt = preparedAt,
            )
        }
    }

    fun approve(
        paymentKey: String,
        method: PaymentMethod,
        approvedAt: LocalDateTime,
    ) {
        require(status == PaymentAttemptStatus.PREPARED) { "준비된 결제 시도만 승인할 수 있습니다." }
        require(paymentKey.isNotBlank()) { "paymentKey는 비어 있을 수 없습니다." }
        require(!approvedAt.isBefore(preparedAt)) { "승인 시각은 준비 시각 이후여야 합니다." }

        status = PaymentAttemptStatus.APPROVED
        this.paymentKey = paymentKey.trim()
        this.method = method
        this.approvedAt = approvedAt
        failureCode = null
        failureMessage = null
        failedAt = null
    }

    fun fail(
        failureCode: String,
        failureMessage: String,
        failedAt: LocalDateTime,
    ) {
        require(status == PaymentAttemptStatus.PREPARED) { "준비된 결제 시도만 실패 처리할 수 있습니다." }
        require(failureCode.isNotBlank()) { "실패 코드는 비어 있을 수 없습니다." }
        require(failureMessage.isNotBlank()) { "실패 메시지는 비어 있을 수 없습니다." }
        require(!failedAt.isBefore(preparedAt)) { "실패 시각은 준비 시각 이후여야 합니다." }

        status = PaymentAttemptStatus.FAILED
        this.failureCode = failureCode.trim()
        this.failureMessage = failureMessage.trim()
        this.failedAt = failedAt
    }
}
