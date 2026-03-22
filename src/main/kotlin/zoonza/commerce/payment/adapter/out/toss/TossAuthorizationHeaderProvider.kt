package zoonza.commerce.payment.adapter.out.toss

import org.springframework.stereotype.Component
import zoonza.commerce.payment.application.port.out.TossPaymentsConfiguration
import java.nio.charset.StandardCharsets
import java.util.Base64

@Component
class TossAuthorizationHeaderProvider(
    private val tossPaymentsConfiguration: TossPaymentsConfiguration,
) {
    fun authorizationHeader(): String {
        val credentials = "${tossPaymentsConfiguration.secretKey}:"
        val encoded = Base64.getEncoder().encodeToString(credentials.toByteArray(StandardCharsets.UTF_8))
        return "Basic $encoded"
    }
}
