package zoonza.commerce.payment.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.order.OrderApi
import zoonza.commerce.order.OrderCanceled
import zoonza.commerce.order.OrderExpired
import zoonza.commerce.payment.PaymentErrorCode
import zoonza.commerce.payment.application.dto.PaymentRedirectResult
import zoonza.commerce.payment.application.dto.PreparePaymentResult
import zoonza.commerce.payment.application.port.`in`.PaymentService
import zoonza.commerce.payment.application.port.out.PaymentGatewayPort
import zoonza.commerce.payment.application.port.out.PaymentRepository
import zoonza.commerce.payment.domain.*
import zoonza.commerce.shared.BusinessException
import java.time.LocalDateTime
import java.util.*

@Service
class DefaultPaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentGatewayPort: PaymentGatewayPort,
    private val paymentUrlFactory: PaymentUrlFactory,
    private val tossPaymentProperties: TossPaymentProperties,
    private val orderApi: OrderApi,
) : PaymentService {
    @Transactional
    override fun prepare(
        memberId: Long,
        orderId: Long,
    ): PreparePaymentResult {
        val pendingOrder = orderApi.findPendingPaymentTarget(orderId)

        if (pendingOrder.memberId != memberId) {
            throw BusinessException(PaymentErrorCode.PAYMENT_PREPARATION_NOT_ALLOWED)
        }

        val payment =
            paymentRepository.findByOrderId(orderId)
                ?.also { existing ->
                    if (existing.status != PaymentStatus.READY) {
                        throw BusinessException(PaymentErrorCode.PAYMENT_PREPARATION_NOT_ALLOWED)
                    }
                }
                ?: paymentRepository.save(
                    Payment.create(
                        orderId = pendingOrder.orderId,
                        orderNumber = pendingOrder.orderNumber,
                        memberId = pendingOrder.memberId,
                        provider = PaymentProvider.TOSS_PAYMENTS,
                        totalAmount = zoonza.commerce.shared.Money(pendingOrder.totalAmount),
                    ),
                )

        val attempt =
            payment.prepareAttempt(
                providerOrderId = "${payment.orderNumber}-${payment.attempts.size + 1}",
                callbackToken = UUID.randomUUID().toString(),
                preparedAt = LocalDateTime.now(),
            )
        val savedPayment = paymentRepository.save(payment)

        return PreparePaymentResult(
            paymentId = savedPayment.id,
            provider = savedPayment.provider,
            clientKey = tossPaymentProperties.clientKey,
            providerOrderId = attempt.providerOrderId,
            orderName = pendingOrder.productNames.firstOrNull()?.let { first ->
                if (pendingOrder.productNames.size == 1) {
                    first
                } else {
                    "$first 외 ${pendingOrder.productNames.size - 1}건"
                }
            } ?: pendingOrder.orderNumber,
            amount = pendingOrder.totalAmount,
            successUrl = paymentUrlFactory.createSuccessCallbackUrl(attempt.callbackToken),
            failUrl = paymentUrlFactory.createFailCallbackUrl(attempt.callbackToken),
        )
    }

    @Transactional
    override fun handleSuccessCallback(
        callbackToken: String,
        providerOrderId: String,
        paymentKey: String,
        amount: Long,
    ): PaymentRedirectResult {
        val payment =
            paymentRepository.findByCallbackToken(callbackToken)
                ?: throw BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND)
        val attempt =
            runCatching { payment.findAttemptByCallbackToken(callbackToken) }
                .getOrElse { throw BusinessException(PaymentErrorCode.PAYMENT_CALLBACK_NOT_ALLOWED) }

        if (attempt.providerOrderId != providerOrderId || payment.totalAmount.amount.longValueExact() != amount) {
            return PaymentRedirectResult(
                paymentUrlFactory.failRedirect(
                    payment = payment,
                    code = PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH.code,
                    message = PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH.message,
                ),
            )
        }

        if (payment.status == PaymentStatus.APPROVED) {
            return PaymentRedirectResult(paymentUrlFactory.successRedirect(payment))
        }

        if (attempt.status != PaymentAttemptStatus.PREPARED) {
            return PaymentRedirectResult(
                paymentUrlFactory.failRedirect(
                    payment = payment,
                    code = attempt.failureCode ?: PaymentErrorCode.PAYMENT_CALLBACK_NOT_ALLOWED.code,
                    message = attempt.failureMessage ?: PaymentErrorCode.PAYMENT_CALLBACK_NOT_ALLOWED.message,
                ),
            )
        }

        val confirmation =
            try {
                paymentGatewayPort.confirm(
                    paymentKey = paymentKey,
                    providerOrderId = providerOrderId,
                    amount = amount,
                    idempotencyKey = "payment-confirm-$callbackToken",
                )
            } catch (e: BusinessException) {
                payment.failAttempt(
                    callbackToken = callbackToken,
                    failureCode = PaymentErrorCode.PAYMENT_PROVIDER_ERROR.code,
                    failureMessage = e.message,
                    failedAt = LocalDateTime.now(),
                )
                paymentRepository.save(payment)
                return PaymentRedirectResult(
                    paymentUrlFactory.failRedirect(
                        payment = payment,
                        code = PaymentErrorCode.PAYMENT_PROVIDER_ERROR.code,
                        message = e.message,
                    ),
                )
            }

        if (confirmation.totalAmount != payment.totalAmount.amount.longValueExact()) {
            payment.failAttempt(
                callbackToken = callbackToken,
                failureCode = PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH.code,
                failureMessage = PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH.message,
                failedAt = LocalDateTime.now(),
            )
            paymentRepository.save(payment)
            return PaymentRedirectResult(
                paymentUrlFactory.failRedirect(
                    payment = payment,
                    code = PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH.code,
                    message = PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH.message,
                ),
            )
        }

        payment.approveAttempt(
            callbackToken = callbackToken,
            paymentKey = confirmation.paymentKey,
            method = confirmation.method.toPaymentMethod(),
            approvedAt = confirmation.approvedAt,
        )
        val savedPayment = paymentRepository.save(payment)
        orderApi.markPaid(savedPayment.orderId)

        return PaymentRedirectResult(paymentUrlFactory.successRedirect(savedPayment))
    }

    @Transactional
    override fun handleFailCallback(
        callbackToken: String,
        code: String,
        message: String,
    ): PaymentRedirectResult {
        val payment =
            paymentRepository.findByCallbackToken(callbackToken)
                ?: throw BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND)

        if (payment.status == PaymentStatus.READY) {
            val attempt = payment.findAttemptByCallbackToken(callbackToken)
            if (attempt.status == PaymentAttemptStatus.PREPARED) {
                payment.failAttempt(
                    callbackToken = callbackToken,
                    failureCode = code,
                    failureMessage = message,
                    failedAt = LocalDateTime.now(),
                )
                paymentRepository.save(payment)
            }
        }

        return PaymentRedirectResult(paymentUrlFactory.failRedirect(payment, code, message))
    }

    @Transactional
    override fun handleWebhook(
        eventType: String,
        paymentKey: String,
        providerOrderId: String?,
    ) {
        if (eventType != "PAYMENT_STATUS_CHANGED" || providerOrderId.isNullOrBlank()) {
            return
        }

        val payment =
            paymentRepository.findByProviderOrderId(providerOrderId)
                ?: return

        if (payment.status != PaymentStatus.READY) {
            return
        }

        val lookup = paymentGatewayPort.get(paymentKey)
        if (lookup.status != "DONE" || lookup.totalAmount != payment.totalAmount.amount.longValueExact()) {
            return
        }

        val attempt = payment.findAttemptByProviderOrderId(providerOrderId)
        payment.approveAttempt(
            callbackToken = attempt.callbackToken,
            paymentKey = lookup.paymentKey,
            method = lookup.method.toPaymentMethod(),
            approvedAt = lookup.approvedAt ?: LocalDateTime.now(),
        )
        paymentRepository.save(payment)
        orderApi.markPaid(payment.orderId)
    }

    @Transactional
    override fun refund(
        orderId: Long,
        reason: String,
    ) {
        val payment =
            paymentRepository.findByOrderId(orderId)
                ?: throw BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND)

        if (payment.status != PaymentStatus.APPROVED) {
            throw BusinessException(PaymentErrorCode.PAYMENT_REFUND_NOT_ALLOWED)
        }

        val cancellation =
            paymentGatewayPort.cancel(
                paymentKey = payment.approvedPaymentKey(),
                cancelReason = reason,
                amount = payment.totalAmount.amount.longValueExact(),
                idempotencyKey = "payment-refund-$orderId",
            )

        payment.refund(
            refundIdempotencyKey = "payment-refund-$orderId",
            reason = reason,
            refundedAt = cancellation.canceledAt,
            providerTransactionKey = cancellation.transactionKey,
        )
        paymentRepository.save(payment)
        orderApi.markRefunded(orderId)
    }

    @Transactional
    fun closeByOrderCancellation(event: OrderCanceled) {
        closePayment(event.orderId, PaymentCloseReason.ORDER_CANCELED)
    }

    @Transactional
    fun closeByOrderExpiration(event: OrderExpired) {
        closePayment(event.orderId, PaymentCloseReason.ORDER_EXPIRED)
    }

    private fun closePayment(
        orderId: Long,
        closeReason: PaymentCloseReason,
    ) {
        val payment = paymentRepository.findByOrderId(orderId) ?: return

        if (payment.status == PaymentStatus.READY) {
            payment.close(closeReason, LocalDateTime.now())
            paymentRepository.save(payment)
        }
    }

    private fun String?.toPaymentMethod(): PaymentMethod {
        return when (this?.uppercase()) {
            "CARD" -> PaymentMethod.CARD
            "TRANSFER" -> PaymentMethod.TRANSFER
            "EASY_PAY" -> PaymentMethod.EASY_PAY
            "MOBILE_PHONE" -> PaymentMethod.MOBILE_PHONE
            else -> PaymentMethod.UNKNOWN
        }
    }
}
