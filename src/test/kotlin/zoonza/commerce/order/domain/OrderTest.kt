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
                orderNumber = "ORD-TEST-1",
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
            orderNumber = "ORD-TEST-2",
            status = OrderStatus.DELIVERED,
            orderedAt = LocalDateTime.of(2026, 3, 21, 10, 0),
            deliveredAt = LocalDateTime.of(2026, 3, 22, 9, 0),
            items = listOf(orderItem()),
        )

        order.items.single().order shouldBe order
        order.items.single().status shouldBe OrderItemStatus.DELIVERED
        order.totalAmount.amount shouldBe 19_900
    }

    @Test
    fun `배송 완료 주문상품은 구매 확정하면 스냅샷과 확정 시각을 저장한다`() {
        val order = Order.create(
            memberId = 1L,
            orderNumber = "ORD-TEST-3",
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

    @Test
    fun `주문 총액은 주문상품 금액과 수량으로 계산한다`() {
        val order = Order.create(
            memberId = 1L,
            orderNumber = "ORD-TEST-4",
            orderedAt = LocalDateTime.of(2026, 3, 21, 10, 0),
            items =
                listOf(
                    orderItem(quantity = 2, orderPrice = Money(19_900)),
                    orderItem(
                        productId = 11L,
                        productOptionId = 21L,
                        productNameSnapshot = "후드티",
                        quantity = 1,
                        orderPrice = Money(39_900),
                    ),
                ),
        )

        order.totalAmount.amount shouldBe 79_700
    }

    @Test
    fun `생성된 주문과 결제 대기 주문만 수정할 수 있다`() {
        val createdOrder = createOrder(status = OrderStatus.CREATED)
        val paymentPendingOrder = createOrder(status = OrderStatus.PAYMENT_PENDING)
        val paidOrder = createOrder(status = OrderStatus.PAID)

        createdOrder.canModify() shouldBe true
        paymentPendingOrder.canModify() shouldBe true
        paidOrder.canModify() shouldBe false
    }

    @Test
    fun `수정 가능한 주문은 주문상품 전체를 교체하고 총액을 다시 계산한다`() {
        val order = createOrder(status = OrderStatus.CREATED)

        order.replaceItems(
            listOf(
                orderItem(
                    productId = 11L,
                    productOptionId = 21L,
                    productNameSnapshot = "후드 집업",
                    quantity = 2,
                    orderPrice = Money(39_900),
                ),
            ),
        )

        order.items.single().productId shouldBe 11L
        order.items.single().status shouldBe OrderItemStatus.CREATED
        order.totalAmount.amount shouldBe 79_800
    }

    @Test
    fun `결제 대기 주문은 삭제하면 취소 상태와 삭제 시각을 기록한다`() {
        val order = createOrder(status = OrderStatus.PAYMENT_PENDING)
        val deletedAt = LocalDateTime.of(2026, 3, 22, 12, 0)

        order.delete(deletedAt)

        order.status shouldBe OrderStatus.CANCELED
        order.deletedAt shouldBe deletedAt
        order.items.single().status shouldBe OrderItemStatus.CANCELED
        order.canDelete() shouldBe false
    }

    @Test
    fun `결제 상태 전이는 주문상품 상태와 함께 변경된다`() {
        val order = createOrder(status = OrderStatus.CREATED)

        order.markPaymentPending()
        order.status shouldBe OrderStatus.PAYMENT_PENDING
        order.items.single().status shouldBe OrderItemStatus.PAYMENT_PENDING

        order.markPaid()
        order.status shouldBe OrderStatus.PAID
        order.items.single().status shouldBe OrderItemStatus.PAID
    }

    @Test
    fun `삭제된 주문은 수정할 수 없다`() {
        val order = createOrder(status = OrderStatus.CREATED)

        order.delete(LocalDateTime.of(2026, 3, 22, 12, 0))

        shouldThrow<IllegalArgumentException> {
            order.replaceItems(listOf(orderItem()))
        }
    }

    private fun createOrder(status: OrderStatus): Order {
        val deliveredAt =
            if (status == OrderStatus.DELIVERED) {
                LocalDateTime.of(2026, 3, 22, 9, 0)
            } else {
                null
            }

        return Order.create(
            memberId = 1L,
            orderNumber = "ORD-STATE-${status.name}",
            status = status,
            orderedAt = LocalDateTime.of(2026, 3, 21, 10, 0),
            deliveredAt = deliveredAt,
            items = listOf(orderItem()),
        )
    }

    private fun orderItem(
        productId: Long = 10L,
        productOptionId: Long = 20L,
        productNameSnapshot: String = "반팔 티셔츠",
        quantity: Int = 1,
        orderPrice: Money = Money(19_900),
    ): OrderItem {
        return OrderItem.create(
            productId = productId,
            productOptionId = productOptionId,
            productNameSnapshot = productNameSnapshot,
            optionColorSnapshot = "BLACK",
            optionSizeSnapshot = "M",
            quantity = quantity,
            orderPrice = orderPrice,
        )
    }
}
