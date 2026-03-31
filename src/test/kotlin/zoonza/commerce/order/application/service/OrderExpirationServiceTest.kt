package zoonza.commerce.order.application.service

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import zoonza.commerce.inventory.InventoryApi
import zoonza.commerce.order.OrderExpired
import zoonza.commerce.order.application.port.out.OrderRepository
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderItem
import zoonza.commerce.order.domain.OrderRecipient
import zoonza.commerce.order.domain.OrderSource
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

class OrderExpirationServiceTest {
    private val orderRepository = mockk<OrderRepository>()
    private val inventoryApi = mockk<InventoryApi>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val service = OrderExpirationService(orderRepository, inventoryApi, eventPublisher)

    @Test
    fun `만료된 결제 대기 주문을 만료 처리한다`() {
        val now = LocalDateTime.of(2026, 4, 1, 12, 0)
        val order = createOrder()
        every { orderRepository.findExpiredPendingOrders(now, any()) } returnsMany listOf(listOf(order), emptyList())
        every { inventoryApi.expireReservation(10L, "ORDER-001", now) } just runs
        every { orderRepository.save(order) } returns order

        val result = service.expirePendingOrders(now)

        result shouldBe 1
        order.expiredAt shouldBe now
        verify(exactly = 1) { eventPublisher.publishEvent(OrderExpired(1L, "ORDER-001", 1L)) }
    }

    private fun createOrder(): Order {
        return Order(
            id = 1L,
            memberId = 1L,
            orderNumber = "ORDER-001",
            source = OrderSource.DIRECT_BUY,
            status = zoonza.commerce.order.domain.OrderStatus.PENDING_PAYMENT,
            orderedAt = LocalDateTime.of(2026, 4, 1, 10, 0),
            expiresAt = LocalDateTime.of(2026, 4, 1, 10, 10),
            recipient =
                OrderRecipient(
                    recipientName = "주문자",
                    recipientPhoneNumber = "01012345678",
                    zipCode = "12345",
                    baseAddress = "서울시 강남구",
                    detailAddress = "",
                ),
            items =
                mutableListOf(
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
            totalAmount = Money(22_000),
        )
    }
}
