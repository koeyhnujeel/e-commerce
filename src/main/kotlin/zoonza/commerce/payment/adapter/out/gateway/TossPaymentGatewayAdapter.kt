package zoonza.commerce.payment.adapter.out.gateway

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import zoonza.commerce.payment.PaymentErrorCode
import zoonza.commerce.payment.application.port.out.PaymentGatewayCancellation
import zoonza.commerce.payment.application.port.out.PaymentGatewayConfirmation
import zoonza.commerce.payment.application.port.out.PaymentGatewayLookup
import zoonza.commerce.payment.application.port.out.PaymentGatewayPort
import zoonza.commerce.payment.application.service.TossPaymentProperties
import zoonza.commerce.shared.BusinessException
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.Base64

@Component
class TossPaymentGatewayAdapter(
    restClientBuilder: RestClient.Builder,
    tossPaymentProperties: TossPaymentProperties,
) : PaymentGatewayPort {
    private val restClient: RestClient =
        restClientBuilder
            .baseUrl(tossPaymentProperties.baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, createAuthorizationHeader(tossPaymentProperties.secretKey))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()

    override fun confirm(
        paymentKey: String,
        providerOrderId: String,
        amount: Long,
        idempotencyKey: String,
    ): PaymentGatewayConfirmation {
        return execute {
            restClient.post()
                .uri("/v1/payments/confirm")
                .header("Idempotency-Key", idempotencyKey)
                .body(
                    ConfirmRequest(
                        paymentKey = paymentKey,
                        orderId = providerOrderId,
                        amount = amount,
                    ),
                )
                .retrieve()
                .body(PaymentResponse::class.java)
                ?.toConfirmation()
                ?: throw BusinessException(PaymentErrorCode.PAYMENT_PROVIDER_ERROR)
        }
    }

    override fun cancel(
        paymentKey: String,
        cancelReason: String,
        amount: Long,
        idempotencyKey: String,
    ): PaymentGatewayCancellation {
        return execute {
            restClient.post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .header("Idempotency-Key", idempotencyKey)
                .body(CancelRequest(cancelReason = cancelReason, cancelAmount = amount))
                .retrieve()
                .body(PaymentResponse::class.java)
                ?.toCancellation()
                ?: throw BusinessException(PaymentErrorCode.PAYMENT_PROVIDER_ERROR)
        }
    }

    override fun get(paymentKey: String): PaymentGatewayLookup {
        return execute {
            restClient.get()
                .uri("/v1/payments/{paymentKey}", paymentKey)
                .retrieve()
                .body(PaymentResponse::class.java)
                ?.toLookup()
                ?: throw BusinessException(PaymentErrorCode.PAYMENT_PROVIDER_ERROR)
        }
    }

    private fun <T> execute(block: () -> T): T {
        return try {
            block()
        } catch (e: RestClientResponseException) {
            throw BusinessException(
                PaymentErrorCode.PAYMENT_PROVIDER_ERROR,
                e.responseBodyAsString.ifBlank { PaymentErrorCode.PAYMENT_PROVIDER_ERROR.message },
                e,
            )
        }
    }

    private fun createAuthorizationHeader(secretKey: String): String {
        val encoded = Base64.getEncoder().encodeToString("$secretKey:".toByteArray(StandardCharsets.UTF_8))
        return "Basic $encoded"
    }

    private fun PaymentResponse.toConfirmation(): PaymentGatewayConfirmation {
        return PaymentGatewayConfirmation(
            paymentKey = paymentKey,
            providerOrderId = orderId,
            totalAmount = totalAmount,
            method = method,
            approvedAt = approvedAt.toLocalDateTimeOrNow(),
        )
    }

    private fun PaymentResponse.toCancellation(): PaymentGatewayCancellation {
        val cancel = cancels?.lastOrNull()
            ?: throw BusinessException(PaymentErrorCode.PAYMENT_PROVIDER_ERROR)

        return PaymentGatewayCancellation(
            paymentKey = paymentKey,
            totalAmount = totalAmount,
            canceledAt = cancel.canceledAt.toLocalDateTimeOrNow(),
            transactionKey = cancel.transactionKey,
        )
    }

    private fun PaymentResponse.toLookup(): PaymentGatewayLookup {
        return PaymentGatewayLookup(
            paymentKey = paymentKey,
            providerOrderId = orderId,
            status = status,
            totalAmount = totalAmount,
            method = method,
            approvedAt = approvedAt?.toLocalDateTimeOrNow(),
        )
    }

    private fun String?.toLocalDateTimeOrNow(): LocalDateTime {
        return this?.let { OffsetDateTime.parse(it).toLocalDateTime() } ?: LocalDateTime.now()
    }

    private data class ConfirmRequest(
        val paymentKey: String,
        val orderId: String,
        val amount: Long,
    )

    private data class CancelRequest(
        val cancelReason: String,
        val cancelAmount: Long,
    )

    private data class PaymentResponse(
        val paymentKey: String,
        val orderId: String,
        val status: String,
        val totalAmount: Long,
        val method: String?,
        val approvedAt: String?,
        val cancels: List<CancelResponse>?,
    )

    private data class CancelResponse(
        val canceledAt: String?,
        val transactionKey: String?,
    )
}
