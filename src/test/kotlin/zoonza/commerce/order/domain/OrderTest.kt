package zoonza.commerce.order.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class OrderTest {
    @Test
    fun `배송 완료 주문은 deliveredAt이 필요하다`() {
        shouldThrow<IllegalArgumentException> {
            Order.create(
                memberId = 1L,
                status = OrderStatus.DELIVERED,
                orderedAt = LocalDateTime.of(2026, 3, 21, 10, 0),
                deliveredAt = null,
                items = listOf(orderItem()),
            )
        }
    }

    @Test
    fun `주문을 생성하면 주문상품이 주문에 연결된다`() {
        val order = Order.create(
            memberId = 1L,
            status = OrderStatus.DELIVERED,
            orderedAt = LocalDateTime.of(2026, 3, 21, 10, 0),
            deliveredAt = LocalDateTime.of(2026, 3, 22, 9, 0),
            items = listOf(orderItem()),
        )

        order.items.single().order shouldBe order
        order.items.single().status shouldBe OrderItemStatus.DELIVERED
    }

    @Test
    fun `배송 완료 주문상품은 구매 확정하면 스냅샷과 확정 시각을 저장한다`() {
        val order = Order.create(
            memberId = 1L,
            status = OrderStatus.DELIVERED,
            orderedAt = LocalDateTime.of(2026, 3, 21, 10, 0),
            deliveredAt = LocalDateTime.of(2026, 3, 22, 9, 0),
            items = listOf(orderItem()),
        )
        val orderItem = order.items.single()
        val confirmedAt = LocalDateTime.of(2026, 3, 23, 11, 0)

        order.confirmPurchase(
            orderItemId = orderItem.id,
            optionColor = "BLACK",
            optionSize = "M",
            confirmedAt = confirmedAt,
        )

        orderItem.status shouldBe OrderItemStatus.PURCHASE_CONFIRMED
        orderItem.confirmedAt shouldBe confirmedAt
        orderItem.optionColorSnapshot shouldBe "BLACK"
        orderItem.optionSizeSnapshot shouldBe "M"
    }

    private fun orderItem(): OrderItem {
        return OrderItem.create(
            productId = 10L,
            productOptionId = 20L,
            quantity = 1,
            orderPrice = Money(19_900),
        )
    }
}
