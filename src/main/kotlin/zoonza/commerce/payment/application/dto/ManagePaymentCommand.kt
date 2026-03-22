package zoonza.commerce.payment.application.dto

data class ConfirmPaymentCommand(
    val paymentKey: String,
    val orderId: String,
    val amount: Long,
)

data class CancelPaymentCommand(
    val reason: String?,
)
