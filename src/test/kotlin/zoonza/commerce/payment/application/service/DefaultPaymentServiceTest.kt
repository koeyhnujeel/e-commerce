package zoonza.commerce.payment.application.service

import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test
import zoonza.commerce.order.OrderApi
import zoonza.commerce.order.PendingPaymentOrder
import zoonza.commerce.payment.application.port.out.*
import zoonza.commerce.payment.domain.*
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class DefaultPaymentServiceTest {
    private val paymentRepository = mockk<PaymentRepository>()
    private val paymentGatewayPort = mockk<PaymentGatewayPort>()
    private val orderApi = mockk<OrderApi>()
    private val paymentUrlFactory =
        PaymentUrlFactory(
            PaymentProperties(
                publicBaseUrl = "https://api.example.com",
                redirect =
                    PaymentProperties.Redirect(
                        successUrl = "https://example.com/payments/success",
                        failUrl = "https://example.com/payments/fail",
                    ),
            ),
        )
    private val paymentService =
        DefaultPaymentService(
            paymentRepository = paymentRepository,
            paymentGatewayPort = paymentGatewayPort,
            paymentUrlFactory = paymentUrlFactory,
            tossPaymentProperties = TossPaymentProperties(
                baseUrl = "https://api.tosspayments.com",
                clientKey = "client-key",
                secretKey = "secret-key",
            ),
            orderApi = orderApi,
        )

    @Test
    fun `결제 준비는 신규 결제와 콜백 URL을 생성한다`() {
        every { orderApi.findPendingPaymentTarget(1L) } returns pendingPaymentOrder()
        every { paymentRepository.findByOrderId(1L) } returns null
        every { paymentRepository.save(any()) } answers {
            val payment = firstArg<Payment>()
            if (payment.id == 0L) payment.withId(100L) else payment
        }

        val result = paymentService.prepare(memberId = 1L, orderId = 1L)

        result.paymentId shouldBe 100L
        result.providerOrderId shouldBe "ORDER-001-1"
        result.clientKey shouldBe "client-key"
        result.successUrl.contains("token=") shouldBe true
    }

    @Test
    fun `성공 콜백은 결제를 승인하고 주문을 결제완료 처리한다`() {
        val payment = readyPayment()
        every { paymentRepository.findByCallbackToken("token-1") } returns payment
        every {
            paymentGatewayPort.confirm("payment-key", "ORDER-001-1", 22_000L, "payment-confirm-token-1")
        } returns
            PaymentGatewayConfirmation(
                paymentKey = "payment-key",
                providerOrderId = "ORDER-001-1",
                totalAmount = 22_000L,
                method = "CARD",
                approvedAt = LocalDateTime.of(2026, 4, 1, 10, 5),
            )
        every { paymentRepository.save(payment) } returns payment
        every { orderApi.markPaid(1L) } just runs

        val result =
            paymentService.handleSuccessCallback(
                callbackToken = "token-1",
                providerOrderId = "ORDER-001-1",
                paymentKey = "payment-key",
                amount = 22_000L,
            )

        payment.status shouldBe PaymentStatus.APPROVED
        payment.attempts.single().status shouldBe PaymentAttemptStatus.APPROVED
        result.redirectUrl shouldBe "https://example.com/payments/success?orderId=1&paymentId=100"
        verify(exactly = 1) { orderApi.markPaid(1L) }
    }

    @Test
    fun `실패 콜백은 시도 이력을 실패로 기록한다`() {
        val payment = readyPayment()
        every { paymentRepository.findByCallbackToken("token-1") } returns payment
        every { paymentRepository.save(payment) } returns payment

        val result =
            paymentService.handleFailCallback(
                callbackToken = "token-1",
                code = "PAY_PROCESS_CANCELED",
                message = "사용자 취소",
            )

        payment.status shouldBe PaymentStatus.READY
        payment.attempts.single().status shouldBe PaymentAttemptStatus.FAILED
        result.redirectUrl shouldBe
            "https://example.com/payments/fail?orderId=1&paymentId=100&code=PAY_PROCESS_CANCELED&message=%EC%82%AC%EC%9A%A9%EC%9E%90%20%EC%B7%A8%EC%86%8C"
    }

    @Test
    fun `웹훅 보정은 DONE 상태를 승인 처리한다`() {
        val payment = readyPayment()
        every { paymentRepository.findByProviderOrderId("ORDER-001-1") } returns payment
        every { paymentGatewayPort.get("payment-key") } returns
            PaymentGatewayLookup(
                paymentKey = "payment-key",
                providerOrderId = "ORDER-001-1",
                status = "DONE",
                totalAmount = 22_000L,
                method = "CARD",
                approvedAt = LocalDateTime.of(2026, 4, 1, 10, 5),
            )
        every { paymentRepository.save(payment) } returns payment
        every { orderApi.markPaid(1L) } just runs

        paymentService.handleWebhook(
            eventType = "PAYMENT_STATUS_CHANGED",
            paymentKey = "payment-key",
            providerOrderId = "ORDER-001-1",
        )

        payment.status shouldBe PaymentStatus.APPROVED
        verify(exactly = 1) { orderApi.markPaid(1L) }
    }

    @Test
    fun `환불은 결제사 취소 후 주문 환불 처리까지 호출한다`() {
        val payment = approvedPayment()
        every { paymentRepository.findByOrderId(1L) } returns payment
        every { paymentGatewayPort.cancel("payment-key", "고객 요청", 22_000L, "payment-refund-1") } returns
            PaymentGatewayCancellation(
                paymentKey = "payment-key",
                totalAmount = 22_000L,
                canceledAt = LocalDateTime.of(2026, 4, 1, 12, 0),
                transactionKey = "tx-1",
            )
        every { paymentRepository.save(payment) } returns payment
        every { orderApi.markRefunded(1L) } just runs

        paymentService.refund(orderId = 1L, reason = "고객 요청")

        payment.status shouldBe PaymentStatus.REFUNDED
        verify(exactly = 1) { orderApi.markRefunded(1L) }
    }

    private fun pendingPaymentOrder(): PendingPaymentOrder {
        return PendingPaymentOrder(
            orderId = 1L,
            orderNumber = "ORDER-001",
            memberId = 1L,
            totalAmount = 22_000L,
            expiresAt = LocalDateTime.of(2026, 4, 1, 10, 10),
            productNames = listOf("상품"),
        )
    }

    private fun readyPayment(): Payment {
        val preparedAt = LocalDateTime.now().minusMinutes(10)
        return Payment(
            id = 100L,
            orderId = 1L,
            orderNumber = "ORDER-001",
            memberId = 1L,
            provider = PaymentProvider.TOSS_PAYMENTS,
            totalAmount = Money(22_000L),
            status = PaymentStatus.READY,
            attempts =
                mutableListOf(
                    PaymentAttempt(
                        id = 1L,
                        attemptNumber = 1,
                        providerOrderId = "ORDER-001-1",
                        callbackToken = "token-1",
                        status = PaymentAttemptStatus.PREPARED,
                        preparedAt = preparedAt,
                    ),
                ),
        )
    }

    private fun approvedPayment(): Payment {
        return Payment(
            id = 100L,
            orderId = 1L,
            orderNumber = "ORDER-001",
            memberId = 1L,
            provider = PaymentProvider.TOSS_PAYMENTS,
            totalAmount = Money(22_000L),
            status = PaymentStatus.APPROVED,
            approvedAt = LocalDateTime.of(2026, 4, 1, 10, 5),
            attempts =
                mutableListOf(
                    PaymentAttempt(
                        id = 1L,
                        attemptNumber = 1,
                        providerOrderId = "ORDER-001-1",
                        callbackToken = "token-1",
                        status = PaymentAttemptStatus.APPROVED,
                        preparedAt = LocalDateTime.of(2026, 4, 1, 10, 0),
                        approvedAt = LocalDateTime.of(2026, 4, 1, 10, 5),
                        paymentKey = "payment-key",
                        method = PaymentMethod.CARD,
                    ),
                ),
        )
    }

    private fun Payment.withId(id: Long): Payment {
        return Payment(
            id = id,
            orderId = orderId,
            orderNumber = orderNumber,
            memberId = memberId,
            provider = provider,
            totalAmount = totalAmount,
            status = status,
            approvedAt = approvedAt,
            refundedAt = refundedAt,
            closedAt = closedAt,
            closeReason = closeReason,
            attempts = attempts.toMutableList(),
            refunds = refunds.toMutableList(),
        )
    }
}
