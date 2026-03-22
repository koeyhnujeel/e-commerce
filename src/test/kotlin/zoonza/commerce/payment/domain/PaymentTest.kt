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
}
