package zoonza.commerce.payment.adapter.out.gateway

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import zoonza.commerce.payment.application.service.TossPaymentProperties
import java.util.*

class TossPaymentGatewayAdapterTest {
    private val restClientBuilder = RestClient.builder()
    private val server = MockRestServiceServer.bindTo(restClientBuilder).build()
    private val adapter =
        TossPaymentGatewayAdapter(
            restClientBuilder = restClientBuilder,
            tossPaymentProperties =
                TossPaymentProperties(
                    baseUrl = "https://api.tosspayments.com",
                    clientKey = "client-key",
                    secretKey = "secret-key",
                ),
        )

    @Test
    fun `결제 승인은 Authorization 과 멱등키를 포함해 요청한다`() {
        server.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", expectedAuthorizationHeader()))
            .andExpect(header("Idempotency-Key", "idem-1"))
            .andExpect(content().json("""{"paymentKey":"payment-key","orderId":"ORDER-001-1","amount":22000}"""))
            .andRespond(
                withSuccess(
                    """
                    {
                      "paymentKey":"payment-key",
                      "orderId":"ORDER-001-1",
                      "status":"DONE",
                      "totalAmount":22000,
                      "method":"CARD",
                      "approvedAt":"2026-04-01T10:05:00+09:00"
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON,
                ),
            )

        val result = adapter.confirm("payment-key", "ORDER-001-1", 22_000L, "idem-1")

        result.paymentKey shouldBe "payment-key"
        result.providerOrderId shouldBe "ORDER-001-1"
        result.totalAmount shouldBe 22_000L
    }

    @Test
    fun `결제 취소는 취소 사유와 금액을 포함해 요청한다`() {
        server.expect(requestTo("https://api.tosspayments.com/v1/payments/payment-key/cancel"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", expectedAuthorizationHeader()))
            .andExpect(header("Idempotency-Key", "idem-2"))
            .andExpect(content().json("""{"cancelReason":"고객 요청","cancelAmount":22000}"""))
            .andRespond(
                withSuccess(
                    """
                    {
                      "paymentKey":"payment-key",
                      "orderId":"ORDER-001-1",
                      "status":"CANCELED",
                      "totalAmount":22000,
                      "method":"CARD",
                      "approvedAt":"2026-04-01T10:05:00+09:00",
                      "cancels":[
                        {
                          "canceledAt":"2026-04-01T12:00:00+09:00",
                          "transactionKey":"tx-1"
                        }
                      ]
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON,
                ),
            )

        val result = adapter.cancel("payment-key", "고객 요청", 22_000L, "idem-2")

        result.transactionKey shouldBe "tx-1"
        result.totalAmount shouldBe 22_000L
    }

    private fun expectedAuthorizationHeader(): String {
        val encoded = Base64.getEncoder().encodeToString("secret-key:".toByteArray())
        return "Basic $encoded"
    }
}
