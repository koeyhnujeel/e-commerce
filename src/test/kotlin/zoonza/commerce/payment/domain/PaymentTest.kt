package zoonza.commerce.payment.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class PaymentTest {
    @Test
    fun `결제 준비는 시도 이력을 누적한다`() {
        val payment = createPayment()

        payment.prepareAttempt("ORDER-001-1", "token-1", LocalDateTime.of(2026, 4, 1, 10, 0))
        payment.prepareAttempt("ORDER-001-2", "token-2", LocalDateTime.of(2026, 4, 1, 10, 1))

        payment.attempts.size shouldBe 2
        payment.attempts[0].attemptNumber shouldBe 1
        payment.attempts[1].attemptNumber shouldBe 2
    }

    @Test
    fun `승인된 결제는 다시 준비할 수 없다`() {
        val payment = createPayment()
        payment.prepareAttempt("ORDER-001-1", "token-1", LocalDateTime.of(2026, 4, 1, 10, 0))
        payment.approveAttempt("token-1", "payment-key", PaymentMethod.CARD, LocalDateTime.of(2026, 4, 1, 10, 2))

        shouldThrow<IllegalArgumentException> {
            payment.prepareAttempt("ORDER-001-2", "token-2", LocalDateTime.of(2026, 4, 1, 10, 3))
        }
    }

    @Test
    fun `승인된 결제는 전액 환불할 수 있다`() {
        val payment = createPayment()
        payment.prepareAttempt("ORDER-001-1", "token-1", LocalDateTime.of(2026, 4, 1, 10, 0))
        payment.approveAttempt("token-1", "payment-key", PaymentMethod.CARD, LocalDateTime.of(2026, 4, 1, 10, 2))

        payment.refund(
            refundIdempotencyKey = "refund-1",
            reason = "고객 요청",
            refundedAt = LocalDateTime.of(2026, 4, 1, 11, 0),
            providerTransactionKey = "tx-1",
        )

        payment.status shouldBe PaymentStatus.REFUNDED
        payment.refunds.single().providerTransactionKey shouldBe "tx-1"
    }

    private fun createPayment(): Payment {
        return Payment.create(
            orderId = 1L,
            orderNumber = "ORDER-001",
            memberId = 1L,
            provider = PaymentProvider.TOSS_PAYMENTS,
            totalAmount = Money(22_000),
        )
    }
}
