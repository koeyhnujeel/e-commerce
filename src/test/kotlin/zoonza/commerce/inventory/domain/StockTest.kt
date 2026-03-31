package zoonza.commerce.inventory.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import zoonza.commerce.shared.BusinessException
import java.time.LocalDateTime

class StockTest {
    @Test
    fun `재고를 생성하면 가용 재고는 총 재고와 같다`() {
        val stock = Stock.create(productOptionId = 101L, totalQuantity = 10L)

        stock.productOptionId shouldBe 101L
        stock.totalQuantity shouldBe 10L
        stock.reservedQuantity shouldBe 0L
        stock.availableQuantity() shouldBe 10L
    }

    @Test
    fun `재고를 예약하면 예약 수량이 증가한다`() {
        val stock = Stock.create(productOptionId = 101L, totalQuantity = 10L)
        val reservedAt = LocalDateTime.of(2026, 3, 31, 10, 0)
        val expiresAt = reservedAt.plusMinutes(10)

        val reservation = stock.reserve("ORDER-001", 3L, reservedAt, expiresAt)

        reservation.status shouldBe StockReservationStatus.RESERVED
        stock.reservedQuantity shouldBe 3L
        stock.availableQuantity() shouldBe 7L
    }

    @Test
    fun `가용 재고보다 많이 예약할 수 없다`() {
        val stock = Stock.create(productOptionId = 101L, totalQuantity = 2L)

        val exception =
            shouldThrow<BusinessException> {
                stock.reserve(
                    orderNumber = "ORDER-001",
                    quantity = 3L,
                    reservedAt = LocalDateTime.of(2026, 3, 31, 10, 0),
                    expiresAt = LocalDateTime.of(2026, 3, 31, 10, 10),
                )
            }

        exception.errorCode shouldBe InventoryErrorCode.INSUFFICIENT_AVAILABLE_STOCK
    }

    @Test
    fun `같은 주문번호로 재고를 중복 예약할 수 없다`() {
        val stock = Stock.create(productOptionId = 101L, totalQuantity = 10L)
        val reservedAt = LocalDateTime.of(2026, 3, 31, 10, 0)
        val expiresAt = reservedAt.plusMinutes(10)
        stock.reserve("ORDER-001", 2L, reservedAt, expiresAt)

        val exception =
            shouldThrow<BusinessException> {
                stock.reserve("ORDER-001", 1L, reservedAt.plusMinutes(1), expiresAt.plusMinutes(1))
            }

        exception.errorCode shouldBe InventoryErrorCode.DUPLICATE_ACTIVE_STOCK_RESERVATION
    }

    @Test
    fun `예약 확정은 총재고와 예약재고를 함께 감소시킨다`() {
        val stock = Stock.create(productOptionId = 101L, totalQuantity = 10L)
        val reservedAt = LocalDateTime.of(2026, 3, 31, 10, 0)
        stock.reserve("ORDER-001", 3L, reservedAt, reservedAt.plusMinutes(10))

        stock.confirmReservation("ORDER-001", reservedAt.plusMinutes(5))

        val reservation = stock.findReservation("ORDER-001")
        reservation.status shouldBe StockReservationStatus.CONFIRMED
        stock.totalQuantity shouldBe 7L
        stock.reservedQuantity shouldBe 0L
        stock.availableQuantity() shouldBe 7L
    }

    @Test
    fun `예약 해제는 총재고는 유지하고 예약재고만 감소시킨다`() {
        val stock = Stock.create(productOptionId = 101L, totalQuantity = 10L)
        val reservedAt = LocalDateTime.of(2026, 3, 31, 10, 0)
        stock.reserve("ORDER-001", 4L, reservedAt, reservedAt.plusMinutes(10))

        stock.releaseReservation("ORDER-001", reservedAt.plusMinutes(2))

        val reservation = stock.findReservation("ORDER-001")
        reservation.status shouldBe StockReservationStatus.RELEASED
        stock.totalQuantity shouldBe 10L
        stock.reservedQuantity shouldBe 0L
        stock.availableQuantity() shouldBe 10L
    }

    @Test
    fun `예약 만료는 총재고는 유지하고 예약재고만 감소시킨다`() {
        val stock = Stock.create(productOptionId = 101L, totalQuantity = 10L)
        val reservedAt = LocalDateTime.of(2026, 3, 31, 10, 0)
        val expiresAt = reservedAt.plusMinutes(10)
        stock.reserve("ORDER-001", 4L, reservedAt, expiresAt)

        stock.expireReservation("ORDER-001", expiresAt)

        val reservation = stock.findReservation("ORDER-001")
        reservation.status shouldBe StockReservationStatus.EXPIRED
        stock.totalQuantity shouldBe 10L
        stock.reservedQuantity shouldBe 0L
        stock.availableQuantity() shouldBe 10L
    }

    @Test
    fun `예약된 재고가 있으면 가용 재고보다 많이 재고를 차감할 수 없다`() {
        val stock = Stock.create(productOptionId = 101L, totalQuantity = 10L)
        val reservedAt = LocalDateTime.of(2026, 3, 31, 10, 0)
        stock.reserve("ORDER-001", 7L, reservedAt, reservedAt.plusMinutes(10))

        val exception =
            shouldThrow<BusinessException> {
                stock.decrease(4L)
            }

        exception.errorCode shouldBe InventoryErrorCode.INSUFFICIENT_AVAILABLE_STOCK
    }

    @Test
    fun `종료된 예약은 다시 전이할 수 없다`() {
        val stock = Stock.create(productOptionId = 101L, totalQuantity = 10L)
        val reservedAt = LocalDateTime.of(2026, 3, 31, 10, 0)
        stock.reserve("ORDER-001", 2L, reservedAt, reservedAt.plusMinutes(10))
        stock.releaseReservation("ORDER-001", reservedAt.plusMinutes(2))

        val exception =
            shouldThrow<BusinessException> {
                stock.confirmReservation("ORDER-001", reservedAt.plusMinutes(3))
            }

        exception.errorCode shouldBe InventoryErrorCode.INVALID_STOCK_RESERVATION_STATUS
    }
}
