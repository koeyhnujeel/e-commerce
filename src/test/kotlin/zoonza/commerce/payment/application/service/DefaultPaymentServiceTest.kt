package zoonza.commerce.payment.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.order.OrderApi
import zoonza.commerce.order.PaymentOrder
import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.payment.adapter.out.toss.TossPaymentsProperties
import zoonza.commerce.payment.adapter.out.toss.TossPaymentsClientException
import zoonza.commerce.payment.application.dto.CancelPaymentCommand
import zoonza.commerce.payment.application.dto.ConfirmPaymentCommand
import zoonza.commerce.payment.application.dto.CreatePaymentCommand
import zoonza.commerce.payment.application.port.out.PaymentRepository
import zoonza.commerce.payment.application.port.out.TossPaymentCancelRequest
import zoonza.commerce.payment.application.port.out.TossPaymentCancelResult
import zoonza.commerce.payment.application.port.out.TossPaymentConfirmRequest
import zoonza.commerce.payment.application.port.out.TossPaymentConfirmResult
import zoonza.commerce.payment.application.port.out.TossPaymentsClient
import zoonza.commerce.payment.domain.Payment
import zoonza.commerce.payment.domain.PaymentMethod
import zoonza.commerce.payment.domain.PaymentStatus
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.ErrorCode
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class DefaultPaymentServiceTest {
    private val paymentRepository = mockk<PaymentRepository>()
    private val orderApi = mockk<OrderApi>()
    private val tossPaymentsClient = mockk<TossPaymentsClient>()
    private val tossPaymentsProperties =
        TossPaymentsProperties().apply {
            baseUrl = "https://api.tosspayments.com"
            clientKey = "test-client-key"
            secretKey = "test-secret-key"
            successUrl = "https://example.com/payments/success"
            failUrl = "https://example.com/payments/fail"
        }
    private val paymentService =
        DefaultPaymentService(
            paymentRepository = paymentRepository,
            orderApi = orderApi,
            tossPaymentsProperties = tossPaymentsProperties,
            tossPaymentsClient = tossPaymentsClient,
        )

    @Test
    fun `결제 생성은 주문 금액을 검증하고 체크아웃 응답을 반환한다`() {
        val savedPayment = slot<Payment>()
        every { orderApi.getPaymentOrder(1L, 10L) } returns paymentOrder()
        every { paymentRepository.existsActiveByOrderId(10L) } returns false
        every { paymentRepository.save(capture(savedPayment)) } answers {
            Payment.create(
                id = 100L,
                orderId = savedPayment.captured.orderId,
                memberId = savedPayment.captured.memberId,
                orderNumber = savedPayment.captured.orderNumber,
                amount = savedPayment.captured.amount,
                paymentMethod = savedPayment.captured.paymentMethod,
                providerReference = savedPayment.captured.providerReference,
                createdAt = savedPayment.captured.createdAt,
            )
        }
        every { orderApi.markPaymentPending(10L) } returns Unit

        val result =
            paymentService.createPayment(
                memberId = 1L,
                orderId = 10L,
                command =
                    CreatePaymentCommand(
                        amount = 39_800,
                        paymentMethod = PaymentMethod.CARD,
                    ),
            )

        result.paymentId shouldBe 100L
        result.status shouldBe PaymentStatus.READY
        result.checkout.orderId shouldBe "ORD-20260322-ABC"
        result.checkout.orderName shouldBe "반팔 티셔츠 외 1건"
        result.checkout.customerKey shouldBe "member-1"
        verify(exactly = 1) { orderApi.markPaymentPending(10L) }
    }

    @Test
    fun `주문 금액과 결제 금액이 다르면 결제를 생성할 수 없다`() {
        every { orderApi.getPaymentOrder(1L, 10L) } returns paymentOrder()

        val exception =
            shouldThrow<BusinessException> {
                paymentService.createPayment(
                    memberId = 1L,
                    orderId = 10L,
                    command =
                        CreatePaymentCommand(
                            amount = 10_000,
                            paymentMethod = PaymentMethod.CARD,
                        ),
                )
            }

        exception.errorCode shouldBe ErrorCode.PAYMENT_AMOUNT_MISMATCH
    }

    @Test
    fun `본인 결제 상세를 조회할 수 있다`() {
        every { paymentRepository.findByIdAndMemberId(100L, 1L) } returns
            Payment.create(
                id = 100L,
                orderId = 10L,
                memberId = 1L,
                orderNumber = "ORD-20260322-ABC",
                amount = Money(39_800),
                paymentMethod = PaymentMethod.CARD,
                providerReference = "ORD-20260322-ABC",
                createdAt = LocalDateTime.of(2026, 3, 22, 12, 0),
            )

        val result = paymentService.getPayment(memberId = 1L, paymentId = 100L)

        result.paymentId shouldBe 100L
        result.orderNumber shouldBe "ORD-20260322-ABC"
        result.amount shouldBe 39_800
    }

    @Test
    fun `결제 확정은 토스 승인 결과를 저장하고 주문을 결제 완료로 바꾼다`() {
        val payment = readyPayment()

        every { paymentRepository.findByIdAndMemberId(100L, 1L) } returns payment
        every {
            tossPaymentsClient.confirm(
                TossPaymentConfirmRequest(
                    paymentKey = "pay_123",
                    orderId = "ORD-20260322-ABC",
                    amount = 39_800,
                ),
            )
        } returns TossPaymentConfirmResult(
            paymentKey = "pay_123",
            method = "CARD",
            providerReference = "tx_123",
            approvedAt = LocalDateTime.of(2026, 3, 22, 12, 5),
        )
        every { paymentRepository.save(payment) } returns payment
        every { orderApi.markPaid(10L) } returns Unit

        val result =
            paymentService.confirmPayment(
                memberId = 1L,
                paymentId = 100L,
                command =
                    ConfirmPaymentCommand(
                        paymentKey = "pay_123",
                        orderId = "ORD-20260322-ABC",
                        amount = 39_800,
                    ),
            )

        result.status shouldBe PaymentStatus.CONFIRMED
        result.paymentKey shouldBe "pay_123"
        result.providerReference shouldBe "tx_123"
        verify(exactly = 1) { orderApi.markPaid(10L) }
    }

    @Test
    fun `토스 승인 실패 시 결제를 실패 처리하고 주문을 결제 가능 상태로 복구한다`() {
        val payment = readyPayment()

        every { paymentRepository.findByIdAndMemberId(100L, 1L) } returns payment
        every { tossPaymentsClient.confirm(any()) } throws TossPaymentsClientException("토스 승인 실패")
        every { paymentRepository.save(payment) } returns payment
        every { orderApi.markPaymentReady(10L) } returns Unit

        val exception =
            shouldThrow<BusinessException> {
                paymentService.confirmPayment(
                    memberId = 1L,
                    paymentId = 100L,
                    command =
                        ConfirmPaymentCommand(
                            paymentKey = "pay_123",
                            orderId = "ORD-20260322-ABC",
                            amount = 39_800,
                        ),
                )
            }

        exception.errorCode shouldBe ErrorCode.EXTERNAL_PAYMENT_REQUEST_FAILED
        payment.status shouldBe PaymentStatus.FAILED
        payment.failureReason shouldBe "토스 승인 실패"
        verify(exactly = 1) { orderApi.markPaymentReady(10L) }
    }

    @Test
    fun `결제 취소는 토스 취소 결과를 저장하고 주문을 취소 상태로 바꾼다`() {
        val payment = readyPayment().apply {
            confirm(
                paymentKey = "pay_123",
                paymentMethod = PaymentMethod.CARD,
                providerReference = "tx_123",
                approvedAt = LocalDateTime.of(2026, 3, 22, 12, 5),
            )
        }

        every { paymentRepository.findByIdAndMemberId(100L, 1L) } returns payment
        every {
            tossPaymentsClient.cancel(
                paymentKey = "pay_123",
                request = TossPaymentCancelRequest(cancelReason = "고객 요청"),
            )
        } returns TossPaymentCancelResult(
            providerReference = "cancel_tx_123",
            cancelReason = "고객 요청",
            canceledAt = LocalDateTime.of(2026, 3, 22, 12, 10),
        )
        every { paymentRepository.save(payment) } returns payment
        every { orderApi.cancel(10L) } returns Unit

        val result =
            paymentService.cancelPayment(
                memberId = 1L,
                paymentId = 100L,
                command = CancelPaymentCommand(reason = "고객 요청"),
            )

        result.status shouldBe PaymentStatus.CANCELED
        result.failureReason shouldBe "고객 요청"
        verify(exactly = 1) { orderApi.cancel(10L) }
    }

    private fun paymentOrder(): PaymentOrder {
        return PaymentOrder(
            orderId = 10L,
            memberId = 1L,
            orderNumber = "ORD-20260322-ABC",
            status = OrderStatus.CREATED,
            totalAmount = Money(39_800),
            productNames = listOf("반팔 티셔츠", "후드 집업"),
        )
    }

    private fun readyPayment(): Payment {
        return Payment.create(
            id = 100L,
            orderId = 10L,
            memberId = 1L,
            orderNumber = "ORD-20260322-ABC",
            amount = Money(39_800),
            paymentMethod = PaymentMethod.CARD,
            providerReference = "ORD-20260322-ABC",
            createdAt = LocalDateTime.of(2026, 3, 22, 12, 0),
        )
    }
}
