package zoonza.commerce.inventory.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import zoonza.commerce.inventory.domain.InventoryErrorCode
import zoonza.commerce.inventory.domain.Stock
import zoonza.commerce.inventory.domain.StockRepository
import zoonza.commerce.shared.BusinessException
import java.time.LocalDateTime

class DefaultInventoryServiceTest {
    private val stockRepository = mockk<StockRepository>()
    private val inventoryService = DefaultInventoryService(stockRepository)

    @Test
    fun `재고 생성에 성공하면 저장된 식별자를 반환한다`() {
        every { stockRepository.findByProductOptionId(101L) } returns null
        every { stockRepository.save(any()) } answers {
            firstArg<Stock>().copyWith(id = 1L)
        }

        val result = inventoryService.createStock(productOptionId = 101L, totalQuantity = 10L)

        result shouldBe 1L
        verify(exactly = 1) { stockRepository.save(any()) }
    }

    @Test
    fun `이미 재고가 있으면 생성에 실패한다`() {
        every { stockRepository.findByProductOptionId(101L) } returns Stock.create(101L, 10L)

        val exception =
            shouldThrow<BusinessException> {
                inventoryService.createStock(productOptionId = 101L, totalQuantity = 10L)
            }

        exception.errorCode shouldBe InventoryErrorCode.DUPLICATE_STOCK
        verify(exactly = 0) { stockRepository.save(any()) }
    }

    @Test
    fun `재고 예약에 성공하면 예약 스냅샷을 반환한다`() {
        val reservedAt = LocalDateTime.of(2026, 3, 31, 10, 0)
        val stock = Stock.create(productOptionId = 101L, totalQuantity = 10L).copyWith(id = 1L)
        val savedStock =
            Stock.create(productOptionId = 101L, totalQuantity = 10L)
                .copyWith(id = 1L)
                .apply {
                    reserve(
                        orderNumber = "ORDER-001",
                        quantity = 2L,
                        reservedAt = reservedAt,
                        expiresAt = reservedAt.plusMinutes(10),
                    )
                    val reservation = reservations.single()
                    reservations[0] =
                        reservation.copyWith(id = 11L)
                }

        every { stockRepository.findByProductOptionId(101L) } returns stock
        every { stockRepository.save(any()) } returns savedStock

        val result =
            inventoryService.reserve(
                productOptionId = 101L,
                orderNumber = "ORDER-001",
                quantity = 2L,
                reservedAt = reservedAt,
                expiresAt = reservedAt.plusMinutes(10),
            )

        result.id shouldBe 11L
        result.productOptionId shouldBe 101L
        result.orderNumber shouldBe "ORDER-001"
        result.quantity shouldBe 2L
    }

    @Test
    fun `없는 재고는 조회할 수 없다`() {
        every { stockRepository.findByProductOptionId(999L) } returns null

        val exception =
            shouldThrow<BusinessException> {
                inventoryService.getStock(999L)
            }

        exception.errorCode shouldBe InventoryErrorCode.STOCK_NOT_FOUND
    }

    @Test
    fun `재고 증가 요청은 수정 후 저장한다`() {
        val stock = Stock.create(productOptionId = 101L, totalQuantity = 10L).copyWith(id = 1L)
        every { stockRepository.findByProductOptionId(101L) } returns stock
        every { stockRepository.save(any()) } returns stock

        inventoryService.increaseStock(productOptionId = 101L, quantity = 5L)

        stock.totalQuantity shouldBe 15L
        verify(exactly = 1) { stockRepository.save(stock) }
    }

    private fun Stock.copyWith(id: Long): Stock {
        return Stock(
            id = id,
            productOptionId = productOptionId,
            totalQuantity = totalQuantity,
            reservedQuantity = reservedQuantity,
            reservations = reservations.toMutableList(),
        )
    }

    private fun zoonza.commerce.inventory.domain.StockReservation.copyWith(id: Long): zoonza.commerce.inventory.domain.StockReservation {
        return zoonza.commerce.inventory.domain.StockReservation(
            id = id,
            orderNumber = orderNumber,
            quantity = quantity,
            status = status,
            reservedAt = reservedAt,
            expiresAt = expiresAt,
            confirmedAt = confirmedAt,
            releasedAt = releasedAt,
            expiredAt = expiredAt,
        )
    }
}
