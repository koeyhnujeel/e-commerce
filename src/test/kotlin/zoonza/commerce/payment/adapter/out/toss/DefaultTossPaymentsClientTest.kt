package zoonza.commerce.payment.adapter.out.toss

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import zoonza.commerce.payment.application.port.out.PaymentCancelRequest
import zoonza.commerce.payment.application.port.out.PaymentConfirmRequest
import zoonza.commerce.payment.application.port.out.PaymentGatewayClientException

class DefaultTossPaymentsClientTest {
    private lateinit var server: MockRestServiceServer
    private lateinit var client: DefaultTossPaymentsClient

    @BeforeEach
    fun setUp() {
        val objectMapper =
            ObjectMapper()
                .registerModule(JavaTimeModule())
                .registerKotlinModule()
        val restClientBuilder =
            RestClient.builder()
                .messageConverters { converters ->
                    converters.removeIf { it is MappingJackson2HttpMessageConverter }
                    converters.add(MappingJackson2HttpMessageConverter(objectMapper))
                }

        server = MockRestServiceServer.bindTo(restClientBuilder).build()
        val properties =
            TossPaymentsProperties().apply {
                baseUrl = "https://api.tosspayments.com"
                clientKey = "test-client-key"
                secretKey = "test-secret-key"
                successUrl = "https://example.com/payments/success"
                failUrl = "https://example.com/payments/fail"
            }
        client =
            DefaultTossPaymentsClient(
                restClientBuilder = restClientBuilder,
                paymentGatewayConfiguration = properties,
                tossAuthorizationHeaderProvider = TossAuthorizationHeaderProvider(properties),
                objectMapper = objectMapper,
            )
    }

    @Test
    fun `토스 승인 요청과 응답을 직렬화 역직렬화한다`() {
        server.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dGVzdC1zZWNyZXQta2V5Og=="))
            .andExpect(content().json("""{"paymentKey":"pay_123","orderId":"ORD-1","amount":19900}"""))
            .andRespond(
                withSuccess(
                    """
                    {
                      "paymentKey": "pay_123",
                      "orderId": "ORD-1",
                      "method": "CARD",
                      "approvedAt": "2026-03-22T12:05:00",
                      "lastTransactionKey": "tx_123"
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON,
                ),
            )

        val result =
            client.confirm(
                PaymentConfirmRequest(
                    paymentKey = "pay_123",
                    orderId = "ORD-1",
                    amount = 19_900,
                ),
            )

        result.paymentKey shouldBe "pay_123"
        result.method shouldBe "CARD"
        result.providerReference shouldBe "tx_123"
        server.verify()
    }

    @Test
    fun `토스 취소 요청과 응답을 직렬화 역직렬화한다`() {
        server.expect(requestTo("https://api.tosspayments.com/v1/payments/pay_123/cancel"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().json("""{"cancelReason":"고객 요청"}"""))
            .andRespond(
                withSuccess(
                    """
                    {
                      "paymentKey": "pay_123",
                      "lastTransactionKey": "tx_123",
                      "cancels": [
                        {
                          "cancelReason": "고객 요청",
                          "canceledAt": "2026-03-22T12:10:00",
                          "transactionKey": "cancel_tx_123"
                        }
                      ]
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON,
                ),
            )

        val result =
            client.cancel(
                paymentKey = "pay_123",
                request = PaymentCancelRequest(cancelReason = "고객 요청"),
            )

        result.providerReference shouldBe "cancel_tx_123"
        result.cancelReason shouldBe "고객 요청"
        server.verify()
    }

    @Test
    fun `토스 오류 응답은 클라이언트 예외로 변환한다`() {
        server.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
            .andRespond(
                withBadRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""{"code":"FAILED","message":"토스 승인 실패"}"""),
            )

        val exception =
            shouldThrow<PaymentGatewayClientException> {
                client.confirm(
                    PaymentConfirmRequest(
                        paymentKey = "pay_123",
                        orderId = "ORD-1",
                        amount = 19_900,
                    ),
                )
            }

        exception.message shouldBe "토스 승인 실패"
    }
}
