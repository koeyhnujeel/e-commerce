package zoonza.commerce.payment.adapter.`in`.request

data class PaymentWebhookRequest(
    val eventType: String,
    val data: PaymentWebhookData,
)

data class PaymentWebhookData(
    val paymentKey: String,
    val orderId: String? = null,
)
