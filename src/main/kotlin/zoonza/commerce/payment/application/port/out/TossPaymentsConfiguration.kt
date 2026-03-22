package zoonza.commerce.payment.application.port.out

interface TossPaymentsConfiguration {
    val baseUrl: String
    val clientKey: String
    val secretKey: String
    val successUrl: String
    val failUrl: String
}
