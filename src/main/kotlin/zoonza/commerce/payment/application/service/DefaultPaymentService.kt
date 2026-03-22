package zoonza.commerce.payment.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.order.OrderApi
import zoonza.commerce.order.PaymentOrder
import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.payment.adapter.out.toss.TossPaymentsProperties
import zoonza.commerce.payment.application.dto.CreatePaymentCommand
import zoonza.commerce.payment.application.dto.CreatePaymentResult
import zoonza.commerce.payment.application.dto.PaymentDetail
import zoonza.commerce.payment.application.dto.TossCheckout
import zoonza.commerce.payment.application.port.`in`.PaymentService
import zoonza.commerce.payment.application.port.out.PaymentRepository
import zoonza.commerce.payment.domain.Payment
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.ErrorCode
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

@Service
class DefaultPaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderApi: OrderApi,
    private val tossPaymentsProperties: TossPaymentsProperties,
) : PaymentService {
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

        return CreatePaymentResult(
            paymentId = payment.id,
            orderId = payment.orderId,
            orderNumber = payment.orderNumber,
            status = payment.status,
            amount = payment.amount.amount,
            checkout =
                TossCheckout(
                    clientKey = tossPaymentsProperties.clientKey,
                    orderId = order.orderNumber,
                    orderName = createOrderName(order),
                    customerKey = createCustomerKey(order.memberId),
                    amount = payment.amount.amount,
                    successUrl = tossPaymentsProperties.successUrl,
                    failUrl = tossPaymentsProperties.failUrl,
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
        if (order.status != OrderStatus.CREATED) {
            throw BusinessException(ErrorCode.PAYMENT_CREATION_NOT_ALLOWED)
        }

        if (order.totalAmount.amount != amount) {
            throw BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH)
        }
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
