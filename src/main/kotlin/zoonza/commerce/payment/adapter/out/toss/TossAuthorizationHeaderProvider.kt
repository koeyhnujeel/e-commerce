package zoonza.commerce.payment.adapter.out.toss

import org.springframework.stereotype.Component
import zoonza.commerce.payment.application.port.out.PaymentGatewayConfiguration
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class TossAuthorizationHeaderProvider(
    private val paymentGatewayConfiguration: PaymentGatewayConfiguration,
) {
    fun authorizationHeader(): String {
        val credentials = "${paymentGatewayConfiguration.secretKey}:"
        val encoded = Base64.getEncoder().encodeToString(credentials.toByteArray(StandardCharsets.UTF_8))
        return "Basic $encoded"
    }
}
