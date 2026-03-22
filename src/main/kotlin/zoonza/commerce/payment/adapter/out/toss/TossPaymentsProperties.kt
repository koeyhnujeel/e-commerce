package zoonza.commerce.payment.adapter.out.toss

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import zoonza.commerce.payment.application.port.out.PaymentGatewayConfiguration

@Component
@ConfigurationProperties(prefix = "payment.toss")
class TossPaymentsProperties : PaymentGatewayConfiguration {
    override lateinit var baseUrl: String
    override lateinit var clientKey: String
    override lateinit var secretKey: String
    override lateinit var successUrl: String
    override lateinit var failUrl: String
}
