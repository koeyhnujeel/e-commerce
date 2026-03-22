package zoonza.commerce.payment.adapter.`in`.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import zoonza.commerce.payment.domain.PaymentMethod

data class CreatePaymentRequest(
    @field:Positive(message = "결제 금액은 1 이상이어야 합니다.")
    val amount: Long,
    @field:NotNull(message = "결제 수단은 필수입니다.")
    val paymentMethod: PaymentMethod,
)
