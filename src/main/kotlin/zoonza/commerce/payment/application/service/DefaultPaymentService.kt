package zoonza.commerce.payment.application.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.order.OrderApi
import zoonza.commerce.order.PaymentOrder
import zoonza.commerce.payment.application.dto.CreatePaymentCommand
import zoonza.commerce.payment.application.dto.CreatePaymentResult
import zoonza.commerce.payment.application.dto.CancelPaymentCommand
import zoonza.commerce.payment.application.dto.ConfirmPaymentCommand
import zoonza.commerce.payment.application.dto.PaymentDetail
import zoonza.commerce.payment.application.dto.TossCheckout
import zoonza.commerce.payment.application.port.`in`.PaymentService
import zoonza.commerce.payment.application.port.out.PaymentRepository
import zoonza.commerce.payment.application.port.out.TossPaymentCancelRequest
import zoonza.commerce.payment.application.port.out.TossPaymentConfirmRequest
import zoonza.commerce.payment.application.port.out.TossPaymentsClient
import zoonza.commerce.payment.application.port.out.TossPaymentsClientException
import zoonza.commerce.payment.application.port.out.TossPaymentsConfiguration
import zoonza.commerce.payment.domain.Payment
import zoonza.commerce.payment.domain.PaymentMethod
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.ErrorCode
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

@Service
class DefaultPaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderApi: OrderApi,
    private val tossPaymentsConfiguration: TossPaymentsConfiguration,
    private val tossPaymentsClient: TossPaymentsClient,
) : PaymentService {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun createPayment(
        memberId: Long,
        orderId: Long,
        command: CreatePaymentCommand,
    ): CreatePaymentResult {
        val order = orderApi.getPaymentOrder(memberId, orderId)
        validateCreatable(order, command.amount)

        if (paymentRepository.existsActiveByOrderId(order.orderId)) {
            throw BusinessException(ErrorCode.ACTIVE_PAYMENT_ALREADY_EXISTS)
        }

        val payment = paymentRepository.save(
            Payment.create(
                orderId = order.orderId,
                memberId = order.memberId,
                orderNumber = order.orderNumber,
                amount = Money(command.amount),
                paymentMethod = command.paymentMethod,
                providerReference = order.orderNumber,
                createdAt = LocalDateTime.now(),
                ),
        )

        orderApi.markPaymentPending(order.orderId)
        log.info(
            "payment created paymentId={} orderId={} status={} amount={}",
            payment.id,
            payment.orderId,
            payment.status,
            payment.amount.amount,
        )

        return CreatePaymentResult(
            paymentId = payment.id,
            orderId = payment.orderId,
            orderNumber = payment.orderNumber,
            status = payment.status,
            amount = payment.amount.amount,
            checkout =
                TossCheckout(
                    clientKey = tossPaymentsConfiguration.clientKey,
                    orderId = order.orderNumber,
                    orderName = createOrderName(order),
                    customerKey = createCustomerKey(order.memberId),
                    amount = payment.amount.amount,
                    successUrl = tossPaymentsConfiguration.successUrl,
                    failUrl = tossPaymentsConfiguration.failUrl,
                ),
            createdAt = payment.createdAt,
        )
    }

    @Transactional(readOnly = true)
    override fun getPayment(
        memberId: Long,
        paymentId: Long,
    ): PaymentDetail {
        val payment = paymentRepository.findByIdAndMemberId(paymentId, memberId)
            ?: throw BusinessException(ErrorCode.PAYMENT_NOT_FOUND)

        return toPaymentDetail(payment)
    }

    @Transactional
    override fun confirmPayment(
        memberId: Long,
        paymentId: Long,
        command: ConfirmPaymentCommand,
    ): PaymentDetail {
        val payment = paymentRepository.findByIdAndMemberId(paymentId, memberId)
            ?: throw BusinessException(ErrorCode.PAYMENT_NOT_FOUND)

        if (!payment.canConfirm()) {
            throw BusinessException(ErrorCode.PAYMENT_CONFIRMATION_NOT_ALLOWED)
        }

        validateConfirmRequest(payment, command)

        return try {
            val confirmedPayment =
                tossPaymentsClient.confirm(
                    TossPaymentConfirmRequest(
                        paymentKey = command.paymentKey,
                        orderId = command.orderId,
                        amount = command.amount,
                    ),
                )

            payment.confirm(
                paymentKey = confirmedPayment.paymentKey,
                paymentMethod = PaymentMethod.fromProvider(confirmedPayment.method),
                providerReference = confirmedPayment.providerReference,
                approvedAt = confirmedPayment.approvedAt ?: LocalDateTime.now(),
            )
            paymentRepository.save(payment)
            orderApi.markPaid(payment.orderId)
            log.info(
                "payment confirmed paymentId={} orderId={} status={} providerReference={}",
                payment.id,
                payment.orderId,
                payment.status,
                payment.providerReference,
            )

            toPaymentDetail(payment)
        } catch (e: TossPaymentsClientException) {
            failPayment(payment, e.message ?: ErrorCode.EXTERNAL_PAYMENT_REQUEST_FAILED.message)
            throw BusinessException(ErrorCode.EXTERNAL_PAYMENT_REQUEST_FAILED, e.message ?: ErrorCode.EXTERNAL_PAYMENT_REQUEST_FAILED.message, e)
        }
    }

    @Transactional
    override fun cancelPayment(
        memberId: Long,
        paymentId: Long,
        command: CancelPaymentCommand,
    ): PaymentDetail {
        val payment = paymentRepository.findByIdAndMemberId(paymentId, memberId)
            ?: throw BusinessException(ErrorCode.PAYMENT_NOT_FOUND)

        if (!payment.canCancel()) {
            throw BusinessException(ErrorCode.PAYMENT_CANCELLATION_NOT_ALLOWED)
        }

        val paymentKey = payment.paymentKey
            ?: throw BusinessException(ErrorCode.PAYMENT_CANCELLATION_NOT_ALLOWED)

        return try {
            val canceledPayment =
                tossPaymentsClient.cancel(
                    paymentKey = paymentKey,
                    request = TossPaymentCancelRequest(cancelReason = command.reason),
                )

            payment.cancel(
                reason = canceledPayment.cancelReason ?: command.reason,
                providerReference = canceledPayment.providerReference,
                canceledAt = canceledPayment.canceledAt ?: LocalDateTime.now(),
            )
            paymentRepository.save(payment)
            orderApi.cancel(payment.orderId)
            log.info(
                "payment canceled paymentId={} orderId={} status={} providerReference={}",
                payment.id,
                payment.orderId,
                payment.status,
                payment.providerReference,
            )

            toPaymentDetail(payment)
        } catch (e: TossPaymentsClientException) {
            throw BusinessException(ErrorCode.EXTERNAL_PAYMENT_REQUEST_FAILED, e.message ?: ErrorCode.EXTERNAL_PAYMENT_REQUEST_FAILED.message, e)
        }
    }

    private fun toPaymentDetail(payment: Payment): PaymentDetail {
        return PaymentDetail(
            paymentId = payment.id,
            orderId = payment.orderId,
            orderNumber = payment.orderNumber,
            status = payment.status,
            paymentMethod = payment.paymentMethod,
            amount = payment.amount.amount,
            paymentKey = payment.paymentKey,
            providerReference = payment.providerReference,
            failureReason = payment.failureReason,
            createdAt = payment.createdAt,
            approvedAt = payment.approvedAt,
            canceledAt = payment.canceledAt,
        )
    }

    private fun validateCreatable(
        order: PaymentOrder,
        amount: Long,
    ) {
        if (!order.payable) {
            throw BusinessException(ErrorCode.PAYMENT_CREATION_NOT_ALLOWED)
        }

        if (order.totalAmount.amount != amount) {
            throw BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH)
        }
    }

    private fun validateConfirmRequest(
        payment: Payment,
        command: ConfirmPaymentCommand,
    ) {
        if (payment.orderNumber != command.orderId) {
            failPayment(payment, "토스 승인 요청의 주문번호가 일치하지 않습니다.")
            throw BusinessException(ErrorCode.PAYMENT_CONFIRMATION_NOT_ALLOWED, "토스 승인 요청의 주문번호가 일치하지 않습니다.")
        }

        if (payment.amount.amount != command.amount) {
            failPayment(payment, ErrorCode.PAYMENT_AMOUNT_MISMATCH.message)
            throw BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH)
        }
    }

    private fun failPayment(
        payment: Payment,
        reason: String,
    ) {
        payment.fail(reason)
        paymentRepository.save(payment)
        orderApi.markPaymentReady(payment.orderId)
        log.warn(
            "payment failed paymentId={} orderId={} status={} reason={}",
            payment.id,
            payment.orderId,
            payment.status,
            reason,
        )
    }

    private fun createOrderName(order: PaymentOrder): String {
        val firstName = order.productNames.firstOrNull()?.trim()
            ?: throw IllegalStateException("주문상품명이 없습니다.")

        return if (order.productNames.size == 1) {
            firstName
        } else {
            "$firstName 외 ${order.productNames.size - 1}건"
        }
    }

    private fun createCustomerKey(memberId: Long): String {
        return "member-$memberId"
    }
}
