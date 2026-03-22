package zoonza.commerce.payment.adapter.out.toss

import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Base64

@Component
class TossAuthorizationHeaderProvider(
    private val tossPaymentsProperties: TossPaymentsProperties,
) {
    fun authorizationHeader(): String {
        val credentials = "${tossPaymentsProperties.secretKey}:"
        val encoded = Base64.getEncoder().encodeToString(credentials.toByteArray(StandardCharsets.UTF_8))
        return "Basic $encoded"
    }
}
