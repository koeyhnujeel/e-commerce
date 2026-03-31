package zoonza.commerce.order.application.service

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import zoonza.commerce.cart.CartApi
import zoonza.commerce.cart.CartOrderItem
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.catalog.ProductOptionSummary
import zoonza.commerce.inventory.InventoryApi
import zoonza.commerce.inventory.StockReservationSnapshot
import zoonza.commerce.inventory.domain.StockReservationStatus
import zoonza.commerce.member.MemberAddressSnapshot
import zoonza.commerce.member.MemberApi
import zoonza.commerce.order.OrderCreated
import zoonza.commerce.order.OrderPaid
import zoonza.commerce.order.application.dto.PlaceCartOrderCommand
import zoonza.commerce.order.application.dto.PlaceDirectOrderCommand
import zoonza.commerce.order.application.port.out.OrderNumberGenerator
import zoonza.commerce.order.application.port.out.OrderRepository
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderSource
import zoonza.commerce.order.domain.OrderStatus
import java.time.LocalDateTime

class DefaultOrderServiceTest {
    private val orderRepository = mockk<OrderRepository>()
    private val orderNumberGenerator = mockk<OrderNumberGenerator>()
    private val catalogApi = mockk<CatalogApi>()
    private val inventoryApi = mockk<InventoryApi>()
    private val memberApi = mockk<MemberApi>()
    private val cartApi = mockk<CartApi>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val orderService =
        DefaultOrderService(
            orderRepository = orderRepository,
            orderNumberGenerator = orderNumberGenerator,
            catalogApi = catalogApi,
            inventoryApi = inventoryApi,
            memberApi = memberApi,
            cartApi = cartApi,
            eventPublisher = eventPublisher,
        )

    @Test
    fun `바로구매 주문을 생성한다`() {
        val savedOrder = slot<Order>()
        every { catalogApi.validateAvailableProductOption(100L, 10L) } just runs
        every { memberApi.findShippingAddress(1L, 5L) } returns shippingAddress()
        every { catalogApi.getProductOptionSummaries(setOf(10L)) } returns
            mapOf(
                10L to productOptionSummary(100L, 10L),
            )
        every { orderNumberGenerator.generate() } returns "ORDER-001"
        every { inventoryApi.reserve(any(), any(), any(), any(), any()) } returns reservationSnapshot()
        every { orderRepository.save(capture(savedOrder)) } answers { savedOrder.captured.withId(1L) }

        val result =
            orderService.placeDirectOrder(
                memberId = 1L,
                command = PlaceDirectOrderCommand(productId = 100L, productOptionId = 10L, quantity = 2L, addressId = 5L),
            )

        result.orderId shouldBe 1L
        result.totalAmount shouldBe 22_000L
        savedOrder.captured.source shouldBe OrderSource.DIRECT_BUY
        verify(exactly = 1) { eventPublisher.publishEvent(OrderCreated(1L, "ORDER-001", 1L)) }
    }

    @Test
    fun `장바구니 주문 생성 후 선택 항목을 제거한다`() {
        val savedOrder = slot<Order>()
        every { cartApi.getSelectedItems(1L, setOf(10L)) } returns
            listOf(CartOrderItem(productId = 100L, productOptionId = 10L, quantity = 2L))
        every { memberApi.findShippingAddress(1L, 5L) } returns shippingAddress()
        every { catalogApi.getProductOptionSummaries(setOf(10L)) } returns mapOf(10L to productOptionSummary(100L, 10L))
        every { inventoryApi.getAvailableQuantities(setOf(10L)) } returns mapOf(10L to 5L)
        every { orderNumberGenerator.generate() } returns "ORDER-002"
        every { inventoryApi.reserve(any(), any(), any(), any(), any()) } returns reservationSnapshot()
        every { orderRepository.save(capture(savedOrder)) } answers { savedOrder.captured.withId(2L) }
        every { cartApi.removeItems(1L, setOf(10L)) } just runs

        val result =
            orderService.placeCartOrder(
                memberId = 1L,
                command = PlaceCartOrderCommand(productOptionIds = setOf(10L), addressId = 5L),
            )

        result.orderId shouldBe 2L
        savedOrder.captured.source shouldBe OrderSource.CART
        verify(exactly = 1) { cartApi.removeItems(1L, setOf(10L)) }
    }

    @Test
    fun `결제 완료 처리 시 재고 예약을 확정한다`() {
        val order = createPersistedOrder(id = 3L, orderNumber = "ORDER-003")
        every { orderRepository.findById(3L) } returns order
        every { inventoryApi.confirmReservation(10L, "ORDER-003", any()) } just runs
        every { orderRepository.save(order) } returns order

        orderService.markPaid(3L)

        order.status shouldBe OrderStatus.PAID
        verify(exactly = 1) { eventPublisher.publishEvent(OrderPaid(3L, "ORDER-003", 1L)) }
    }

    private fun shippingAddress(): MemberAddressSnapshot {
        return MemberAddressSnapshot(
            id = 5L,
            label = "집",
            recipientName = "주문자",
            recipientPhoneNumber = "01012345678",
            zipCode = "12345",
            baseAddress = "서울시 강남구",
            detailAddress = "",
            isDefault = true,
        )
    }

    private fun productOptionSummary(
        productId: Long,
        optionId: Long,
    ): ProductOptionSummary {
        return ProductOptionSummary(
            productId = productId,
            productOptionId = optionId,
            productName = "상품",
            primaryImageUrl = "https://cdn.example.com/product.jpg",
            basePrice = 10_000L,
            additionalPrice = 1_000L,
            color = "BLACK",
            size = "M",
            availableForSale = true,
        )
    }

    private fun reservationSnapshot(): StockReservationSnapshot {
        return StockReservationSnapshot(
            id = 1L,
            productOptionId = 10L,
            orderNumber = "ORDER-001",
            quantity = 2L,
            status = StockReservationStatus.RESERVED,
            reservedAt = LocalDateTime.of(2026, 4, 1, 10, 0),
            expiresAt = LocalDateTime.of(2026, 4, 1, 10, 10),
            confirmedAt = null,
            releasedAt = null,
            expiredAt = null,
        )
    }

    private fun createPersistedOrder(
        id: Long,
        orderNumber: String,
    ): Order {
        return Order.create(
            memberId = 1L,
            orderNumber = orderNumber,
            source = OrderSource.DIRECT_BUY,
            orderedAt = LocalDateTime.of(2026, 4, 1, 10, 0),
            expiresAt = LocalDateTime.of(2026, 4, 1, 10, 10),
            recipient =
                zoonza.commerce.order.domain.OrderRecipient(
                    recipientName = "주문자",
                    recipientPhoneNumber = "01012345678",
                    zipCode = "12345",
                    baseAddress = "서울시 강남구",
                    detailAddress = "",
                ),
            items =
                listOf(
                    zoonza.commerce.order.domain.OrderItem(
                        productId = 100L,
                        productOptionId = 10L,
                        productName = "상품",
                        primaryImageUrl = null,
                        optionColor = "BLACK",
                        optionSize = "M",
                        unitPrice = zoonza.commerce.shared.Money(11_000),
                        quantity = 2L,
                    ),
                ),
        ).withId(id)
    }

    private fun Order.withId(id: Long): Order {
        return Order(
            id = id,
            memberId = memberId,
            orderNumber = orderNumber,
            source = source,
            status = status,
            orderedAt = orderedAt,
            expiresAt = expiresAt,
            recipient = recipient,
            items = items,
            totalAmount = totalAmount,
            canceledAt = canceledAt,
            expiredAt = expiredAt,
            paidAt = paidAt,
        )
    }
}
