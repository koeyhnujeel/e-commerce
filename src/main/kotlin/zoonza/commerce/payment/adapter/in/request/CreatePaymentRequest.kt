package zoonza.commerce.payment.adapter.`in`.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import zoonza.commerce.payment.domain.PaymentMethod

data class CreatePaymentRequest(
    @field:Positive(message = "결제 금액은 1 이상이어야 합니다.")
    val amount: Long,
    @field:NotNull(message = "결제 수단은 필수입니다.")
    val paymentMethod: PaymentMethod,
)

data class ConfirmPaymentRequest(
    @field:Positive(message = "결제 금액은 1 이상이어야 합니다.")
    val amount: Long,
    @field:NotBlank(message = "주문번호는 비어 있을 수 없습니다.")
    val orderId: String,
    @field:NotBlank(message = "결제 키는 비어 있을 수 없습니다.")
    val paymentKey: String,
)

data class CancelPaymentRequest(
    val reason: String?,
)
