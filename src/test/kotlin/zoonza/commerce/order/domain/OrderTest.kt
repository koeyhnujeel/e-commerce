package zoonza.commerce.order.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class OrderTest {
    @Test
    fun `주문 생성 시 총액을 계산한다`() {
        val order = createOrder()

        order.totalAmount shouldBe Money(22_000)
    }

    @Test
    fun `결제 대기 주문은 취소할 수 있다`() {
        val order = createOrder()
        val canceledAt = LocalDateTime.of(2026, 4, 1, 12, 0)

        order.cancel(canceledAt)

        order.status shouldBe OrderStatus.CANCELED
        order.canceledAt shouldBe canceledAt
    }

    @Test
    fun `결제 완료된 주문은 다시 취소할 수 없다`() {
        val order = createOrder()
        order.markPaid(LocalDateTime.of(2026, 4, 1, 11, 0))

        shouldThrow<IllegalArgumentException> {
            order.cancel(LocalDateTime.of(2026, 4, 1, 12, 0))
        }
    }

    @Test
    fun `결제 완료된 주문은 환불 처리할 수 있다`() {
        val order = createOrder()
        val paidAt = LocalDateTime.of(2026, 4, 1, 11, 0)
        val refundedAt = LocalDateTime.of(2026, 4, 1, 12, 0)
        order.markPaid(paidAt)

        order.refund(refundedAt)

        order.status shouldBe OrderStatus.REFUNDED
        order.refundedAt shouldBe refundedAt
    }

    private fun createOrder(): Order {
        val orderedAt = LocalDateTime.of(2026, 4, 1, 10, 0)
        return Order.create(
            memberId = 1L,
            orderNumber = "ORDER-001",
            source = OrderSource.DIRECT_BUY,
            orderedAt = orderedAt,
            expiresAt = orderedAt.plusMinutes(10),
            recipient =
                OrderRecipient(
                    recipientName = "주문자",
                    recipientPhoneNumber = "01012345678",
                    zipCode = "12345",
                    baseAddress = "서울시 강남구",
                    detailAddress = "",
                ),
            items =
                listOf(
                    OrderItem(
                        productId = 100L,
                        productOptionId = 10L,
                        productName = "상품",
                        primaryImageUrl = null,
                        optionColor = "BLACK",
                        optionSize = "M",
                        unitPrice = Money(11_000),
                        quantity = 2L,
                    ),
                ),
        )
    }
}
