package zoonza.commerce.payment.application.service

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "payment")
data class PaymentProperties(
    val publicBaseUrl: String,
    val redirect: Redirect,
) {
    data class Redirect(
        val successUrl: String,
        val failUrl: String,
    )
}
