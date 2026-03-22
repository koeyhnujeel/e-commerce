package zoonza.commerce.payment.adapter.out.toss

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "payment.toss")
class TossPaymentsProperties {
    lateinit var baseUrl: String
    lateinit var clientKey: String
    lateinit var secretKey: String
    lateinit var successUrl: String
    lateinit var failUrl: String
}
