package zoonza.commerce.payment.application.service

import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import zoonza.commerce.payment.domain.Payment

@Component
class PaymentUrlFactory(
    private val paymentProperties: PaymentProperties,
) {
    fun createSuccessCallbackUrl(callbackToken: String): String {
        return UriComponentsBuilder
            .fromUriString("${paymentProperties.publicBaseUrl}/api/payments/toss/success")
            .queryParam("token", callbackToken)
            .build()
            .encode()
            .toUriString()
    }

    fun createFailCallbackUrl(callbackToken: String): String {
        return UriComponentsBuilder
            .fromUriString("${paymentProperties.publicBaseUrl}/api/payments/toss/fail")
            .queryParam("token", callbackToken)
            .build()
            .encode()
            .toUriString()
    }

    fun successRedirect(payment: Payment): String {
        return UriComponentsBuilder
            .fromUriString(paymentProperties.redirect.successUrl)
            .queryParam("orderId", payment.orderId)
            .queryParam("paymentId", payment.id)
            .build()
            .encode()
            .toUriString()
    }

    fun failRedirect(
        payment: Payment,
        code: String,
        message: String,
    ): String {
        return UriComponentsBuilder
            .fromUriString(paymentProperties.redirect.failUrl)
            .queryParam("orderId", payment.orderId)
            .queryParam("paymentId", payment.id)
            .queryParam("code", code)
            .queryParam("message", message)
            .build()
            .encode()
            .toUriString()
    }
}
