package zoonza.commerce.payment.application.port.out

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

interface TossPaymentsClient {
    fun confirm(request: TossPaymentConfirmRequest): TossPaymentConfirmResult

    fun cancel(
        paymentKey: String,
        request: TossPaymentCancelRequest,
    ): TossPaymentCancelResult
}

data class TossPaymentConfirmRequest(
    val paymentKey: String,
    val orderId: String,
    val amount: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TossPaymentConfirmResult(
    val paymentKey: String,
    val method: String?,
    val providerReference: String,
    val approvedAt: LocalDateTime?,
)

data class TossPaymentCancelRequest(
    val cancelReason: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TossPaymentCancelResult(
    val providerReference: String,
    val cancelReason: String?,
    val canceledAt: LocalDateTime?,
)
