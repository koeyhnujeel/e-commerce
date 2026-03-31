package zoonza.commerce.payment.application.service

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "payment.toss")
data class TossPaymentProperties(
    val baseUrl: String,
    val clientKey: String,
    val secretKey: String,
)
