package zoonza.commerce.order.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.catalog.OrderProductSnapshot
import zoonza.commerce.order.application.dto.CreateOrderCommand
import zoonza.commerce.order.application.dto.CreateOrderItemCommand
import zoonza.commerce.order.application.dto.UpdateOrderCommand
import zoonza.commerce.order.application.dto.UpdateOrderItemCommand
import zoonza.commerce.order.application.port.out.OrderNumberGenerator
import zoonza.commerce.order.application.port.out.OrderRepository
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderItem
import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.ErrorCode
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class DefaultOrderServiceTest {
    private val orderRepository = mockk<OrderRepository>()
    private val catalogApi = mockk<CatalogApi>()
    private val orderNumberGenerator = mockk<OrderNumberGenerator>()
    private val orderService =
        DefaultOrderService(
            orderRepository = orderRepository,
            catalogApi = catalogApi,
            orderNumberGenerator = orderNumberGenerator,
        )

    @Test
    fun `주문 생성은 서버 계산 금액과 상품 스냅샷으로 저장한다`() {
        val savedOrder = slot<Order>()

        every { orderNumberGenerator.generate() } returns "ORD-TEST-100"
        every {
            catalogApi.findOrderProductSnapshot(10L, 20L)
        } returns OrderProductSnapshot(
            productName = "반팔 티셔츠",
            optionColor = "BLACK",
            optionSize = "M",
            unitPrice = Money(19_900),
        )
        every {
            catalogApi.findOrderProductSnapshot(11L, 21L)
        } returns OrderProductSnapshot(
            productName = "후드 티셔츠",
            optionColor = "GRAY",
            optionSize = "L",
            unitPrice = Money(39_900),
        )
        every { orderRepository.save(capture(savedOrder)) } answers { savedOrder.captured }

        val result = orderService.createOrder(
            memberId = 1L,
            command =
                CreateOrderCommand(
                    items =
                        listOf(
                            CreateOrderItemCommand(productId = 10L, productOptionId = 20L, quantity = 2),
                            CreateOrderItemCommand(productId = 11L, productOptionId = 21L, quantity = 1),
                        ),
                ),
        )

        result.orderNumber shouldBe "ORD-TEST-100"
        result.totalAmount shouldBe 79_700
        savedOrder.captured.status shouldBe OrderStatus.CREATED
        savedOrder.captured.items.map(OrderItem::productNameSnapshot) shouldBe listOf("반팔 티셔츠", "후드 티셔츠")
        savedOrder.captured.items.map(OrderItem::optionColorSnapshot) shouldBe listOf("BLACK", "GRAY")
    }

    @Test
    fun `내 주문 목록 조회는 최신순 주문 요약을 반환한다`() {
        every { orderRepository.findOrders(1L) } returns
            listOf(
                order(id = 2L, orderNumber = "ORD-2", orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0)),
                order(id = 1L, orderNumber = "ORD-1", orderedAt = LocalDateTime.of(2026, 3, 21, 10, 0)),
            )

        val result = orderService.getOrders(1L)

        result.map { it.orderId } shouldBe listOf(2L, 1L)
        result.map { it.totalAmount } shouldBe listOf(19_900L, 19_900L)
    }

    @Test
    fun `내 주문 상세를 찾지 못하면 예외를 던진다`() {
        every { orderRepository.findOrderByIdAndMemberId(10L, 1L) } returns null

        val exception =
            shouldThrow<BusinessException> {
                orderService.getOrder(memberId = 1L, orderId = 10L)
            }

        exception.errorCode shouldBe ErrorCode.ORDER_NOT_FOUND
    }

    @Test
    fun `내 주문 상세 조회는 주문상품과 총액을 반환한다`() {
        every { orderRepository.findOrderByIdAndMemberId(10L, 1L) } returns
            Order.create(
                id = 10L,
                memberId = 1L,
                orderNumber = "ORD-10",
                status = OrderStatus.CREATED,
                orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                items =
                    listOf(
                        OrderItem.create(
                            productId = 10L,
                            productOptionId = 20L,
                            productNameSnapshot = "반팔 티셔츠",
                            optionColorSnapshot = "BLACK",
                            optionSizeSnapshot = "M",
                            quantity = 2,
                            orderPrice = Money(19_900),
                        ),
                    ),
            )

        val result = orderService.getOrder(memberId = 1L, orderId = 10L)

        result.orderId shouldBe 10L
        result.totalAmount shouldBe 39_800
        result.items.single().productName shouldBe "반팔 티셔츠"
        result.items.single().lineAmount shouldBe 39_800
    }

    @Test
    fun `주문 수정은 주문상품을 다시 검증하고 총액을 갱신한다`() {
        val savedOrder = slot<Order>()
        val existingOrder = order(id = 10L, orderNumber = "ORD-10", orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0))

        every { orderRepository.findOrderByIdAndMemberId(10L, 1L) } returns existingOrder
        every {
            catalogApi.findOrderProductSnapshot(11L, 21L)
        } returns OrderProductSnapshot(
            productName = "후드 집업",
            optionColor = "GRAY",
            optionSize = "L",
            unitPrice = Money(39_900),
        )
        every { orderRepository.save(capture(savedOrder)) } answers { savedOrder.captured }

        val result = orderService.updateOrder(
            memberId = 1L,
            orderId = 10L,
            command =
                UpdateOrderCommand(
                    items =
                        listOf(
                            UpdateOrderItemCommand(productId = 11L, productOptionId = 21L, quantity = 2),
                        ),
                ),
        )

        result.totalAmount shouldBe 79_800
        savedOrder.captured.items.single().productNameSnapshot shouldBe "후드 집업"
        savedOrder.captured.items.single().status shouldBe zoonza.commerce.order.domain.OrderItemStatus.CREATED
    }

    @Test
    fun `결제 완료 주문은 수정할 수 없다`() {
        every { orderRepository.findOrderByIdAndMemberId(10L, 1L) } returns
            order(
                id = 10L,
                orderNumber = "ORD-10",
                orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                status = OrderStatus.PAID,
            )

        val exception =
            shouldThrow<BusinessException> {
                orderService.updateOrder(
                    memberId = 1L,
                    orderId = 10L,
                    command = UpdateOrderCommand(items = listOf(UpdateOrderItemCommand(10L, 20L, 1))),
                )
            }

        exception.errorCode shouldBe ErrorCode.ORDER_MODIFICATION_NOT_ALLOWED
    }

    @Test
    fun `주문 삭제는 취소 상태와 삭제 시각을 저장한다`() {
        val savedOrder = slot<Order>()
        val existingOrder = order(id = 10L, orderNumber = "ORD-10", orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0))

        every { orderRepository.findOrderByIdAndMemberId(10L, 1L) } returns existingOrder
        every { orderRepository.save(capture(savedOrder)) } answers { savedOrder.captured }

        orderService.deleteOrder(memberId = 1L, orderId = 10L)

        savedOrder.captured.status shouldBe OrderStatus.CANCELED
        savedOrder.captured.deletedAt.shouldNotBeNull()
    }

    @Test
    fun `결제 완료 주문은 삭제할 수 없다`() {
        every { orderRepository.findOrderByIdAndMemberId(10L, 1L) } returns
            order(
                id = 10L,
                orderNumber = "ORD-10",
                orderedAt = LocalDateTime.of(2026, 3, 22, 10, 0),
                status = OrderStatus.PAID,
            )

        val exception =
            shouldThrow<BusinessException> {
                orderService.deleteOrder(memberId = 1L, orderId = 10L)
            }

        exception.errorCode shouldBe ErrorCode.ORDER_DELETION_NOT_ALLOWED
    }

    private fun order(
        id: Long,
        orderNumber: String,
        orderedAt: LocalDateTime,
        status: OrderStatus = OrderStatus.CREATED,
    ): Order {
        val deliveredAt =
            if (status == OrderStatus.DELIVERED) {
                orderedAt.plusDays(1)
            } else {
                null
            }

        return Order.create(
            id = id,
            memberId = 1L,
            orderNumber = orderNumber,
            status = status,
            orderedAt = orderedAt,
            deliveredAt = deliveredAt,
            items =
                listOf(
                    OrderItem.create(
                        productId = 10L,
                        productOptionId = 20L,
                        productNameSnapshot = "반팔 티셔츠",
                        optionColorSnapshot = "BLACK",
                        optionSizeSnapshot = "M",
                        quantity = 1,
                        orderPrice = Money(19_900),
                    ),
                ),
        )
    }
}
