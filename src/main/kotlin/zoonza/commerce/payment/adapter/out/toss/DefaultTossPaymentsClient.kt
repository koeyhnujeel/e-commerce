package zoonza.commerce.payment.adapter.out.toss

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import zoonza.commerce.payment.application.port.out.*
import java.time.LocalDateTime

@Component
class DefaultTossPaymentsClient(
    restClientBuilder: RestClient.Builder,
    private val paymentGatewayConfiguration: PaymentGatewayConfiguration,
    private val tossAuthorizationHeaderProvider: TossAuthorizationHeaderProvider,
    private val objectMapper: ObjectMapper,
) : PaymentGatewayClient {
    private val log = LoggerFactory.getLogger(javaClass)

    private val restClient =
        restClientBuilder
            .baseUrl(paymentGatewayConfiguration.baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, tossAuthorizationHeaderProvider.authorizationHeader())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()

    override fun confirm(request: PaymentConfirmRequest): PaymentConfirmResult {
        return try {
            val response =
                restClient.post()
                    .uri("/v1/payments/confirm")
                    .body(request)
                    .retrieve()
                    .body(TossConfirmResponse::class.java)
                    ?: throw PaymentGatewayClientException("토스 승인 응답 본문이 비어 있습니다.")

            PaymentConfirmResult(
                paymentKey = response.paymentKey,
                method = response.method,
                providerReference = response.lastTransactionKey ?: response.mId ?: response.orderId,
                approvedAt = response.approvedAt,
            )
        } catch (e: RestClientResponseException) {
            log.warn(
                "toss confirm failed status={} orderId={} paymentKey={} message={}",
                e.statusCode.value(),
                mask(request.orderId),
                mask(request.paymentKey),
                extractMessage(e),
            )
            throw PaymentGatewayClientException(extractMessage(e), e)
        } catch (e: RestClientException) {
            log.warn(
                "toss confirm request failed orderId={} paymentKey={} message={}",
                mask(request.orderId),
                mask(request.paymentKey),
                e.message,
            )
            throw PaymentGatewayClientException("토스 결제 승인 호출에 실패했습니다.", e)
        }
    }

    override fun cancel(
        paymentKey: String,
        request: PaymentCancelRequest,
    ): PaymentCancelResult {
        return try {
            val response =
                restClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .body(request)
                    .retrieve()
                    .body(TossCancelResponse::class.java)
                    ?: throw PaymentGatewayClientException("토스 취소 응답 본문이 비어 있습니다.")

            val latestCancel = response.cancels.maxByOrNull { it.canceledAt ?: LocalDateTime.MIN }

            PaymentCancelResult(
                providerReference = latestCancel?.transactionKey ?: response.lastTransactionKey ?: response.paymentKey,
                cancelReason = latestCancel?.cancelReason,
                canceledAt = latestCancel?.canceledAt,
            )
        } catch (e: RestClientResponseException) {
            log.warn(
                "toss cancel failed status={} paymentKey={} message={}",
                e.statusCode.value(),
                mask(paymentKey),
                extractMessage(e),
            )
            throw PaymentGatewayClientException(extractMessage(e), e)
        } catch (e: RestClientException) {
            log.warn(
                "toss cancel request failed paymentKey={} message={}",
                mask(paymentKey),
                e.message,
            )
            throw PaymentGatewayClientException("토스 결제 취소 호출에 실패했습니다.", e)
        }
    }

    private fun extractMessage(exception: RestClientResponseException): String {
        val responseBody = exception.responseBodyAsString
        if (responseBody.isBlank()) {
            return "토스 결제 요청이 실패했습니다."
        }

        return runCatching {
            val payload: JsonNode = objectMapper.readTree(responseBody)
            payload.path("message").asText().ifBlank { "토스 결제 요청이 실패했습니다." }
        }.getOrDefault("토스 결제 요청이 실패했습니다.")
    }

    private fun mask(value: String): String {
        if (value.length <= 4) {
            return "****"
        }
        return "${value.take(2)}***${value.takeLast(2)}"
    }

    private data class TossConfirmResponse(
        val mId: String? = null,
        val paymentKey: String,
        val orderId: String,
        val method: String? = null,
        val approvedAt: LocalDateTime? = null,
        val lastTransactionKey: String? = null,
    )

    private data class TossCancelResponse(
        val paymentKey: String,
        val lastTransactionKey: String? = null,
        val cancels: List<TossCancelEntry> = emptyList(),
    )

    private data class TossCancelEntry(
        val cancelReason: String? = null,
        val canceledAt: LocalDateTime? = null,
        val transactionKey: String? = null,
    )
}
