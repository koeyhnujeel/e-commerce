package zoonza.commerce.payment.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class PaymentTest {
    @Test
    fun `결제를 생성하면 활성 결제와 토스 참조값을 기록한다`() {
        val payment =
            Payment.create(
                orderId = 1L,
                memberId = 2L,
                orderNumber = "ORD-TEST-1",
                amount = Money(39_800),
                paymentMethod = PaymentMethod.CARD,
                providerReference = "ORD-TEST-1",
                createdAt = LocalDateTime.of(2026, 3, 22, 12, 0),
            )

        payment.status shouldBe PaymentStatus.READY
        payment.paymentMethod shouldBe PaymentMethod.CARD
        payment.providerReference shouldBe "ORD-TEST-1"
        payment.isActive() shouldBe true
    }

    @Test
    fun `결제 승인과 취소는 상태와 참조값을 갱신한다`() {
        val payment =
            Payment.create(
                orderId = 1L,
                memberId = 2L,
                orderNumber = "ORD-TEST-2",
                amount = Money(39_800),
                paymentMethod = PaymentMethod.UNKNOWN,
                providerReference = "ORD-TEST-2",
                createdAt = LocalDateTime.of(2026, 3, 22, 12, 0),
            )

        payment.confirm(
            paymentKey = "pay_123",
            paymentMethod = PaymentMethod.CARD,
            providerReference = "tx_123",
            approvedAt = LocalDateTime.of(2026, 3, 22, 12, 5),
        )
        payment.cancel(
            reason = "고객 요청",
            providerReference = "cancel_tx_123",
            canceledAt = LocalDateTime.of(2026, 3, 22, 12, 10),
        )

        payment.status shouldBe PaymentStatus.CANCELED
        payment.paymentMethod shouldBe PaymentMethod.CARD
        payment.providerReference shouldBe "cancel_tx_123"
        payment.failureReason shouldBe "고객 요청"
        payment.isActive() shouldBe false
    }
}
