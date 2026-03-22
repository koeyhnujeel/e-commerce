package zoonza.commerce.payment.application.port.out

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

interface PaymentGatewayClient {
    fun confirm(request: PaymentConfirmRequest): PaymentConfirmResult

    fun cancel(
        paymentKey: String,
        request: PaymentCancelRequest,
    ): PaymentCancelResult
}

data class PaymentConfirmRequest(
    val paymentKey: String,
    val orderId: String,
    val amount: Long,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentConfirmResult(
    val paymentKey: String,
    val method: String?,
    val providerReference: String,
    val approvedAt: LocalDateTime?,
)

data class PaymentCancelRequest(
    val cancelReason: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentCancelResult(
    val providerReference: String,
    val cancelReason: String?,
    val canceledAt: LocalDateTime?,
)
