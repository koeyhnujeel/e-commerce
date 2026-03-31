package zoonza.commerce.inventory.domain

import zoonza.commerce.shared.BusinessException
import java.time.LocalDateTime

class Stock(
    val id: Long = 0L,
    val productOptionId: Long,
    var totalQuantity: Long,
    var reservedQuantity: Long,
    val reservations: MutableList<StockReservation> = mutableListOf()
) {
    companion object {
        fun create(
            productOptionId: Long,
            totalQuantity: Long,
        ): Stock {
            require(productOptionId > 0) { "상품 옵션 ID는 1 이상이어야 합니다." }
            require(totalQuantity > 0) { "재고 수량은 1개 이상이어야 합니다." }

            return Stock(
                productOptionId = productOptionId,
                totalQuantity = totalQuantity,
                reservedQuantity = 0,
            )
        }
    }

    fun availableQuantity(): Long = totalQuantity - reservedQuantity

    fun increase(quantity: Long) {
        requirePositiveQuantity(quantity)
        totalQuantity += quantity
    }

    fun decrease(quantity: Long) {
        requirePositiveQuantity(quantity)

        if (availableQuantity() < quantity) {
            throw BusinessException(InventoryErrorCode.INSUFFICIENT_AVAILABLE_STOCK)
        }

        totalQuantity -= quantity
    }

    fun reserve(
        orderNumber: String,
        quantity: Long,
        reservedAt: LocalDateTime,
        expiresAt: LocalDateTime,
    ): StockReservation {
        requirePositiveQuantity(quantity)

        if (reservations.any { it.orderNumber == orderNumber }) {
            throw BusinessException(InventoryErrorCode.DUPLICATE_STOCK_RESERVATION)
        }

        if (availableQuantity() < quantity) {
            throw BusinessException(InventoryErrorCode.INSUFFICIENT_AVAILABLE_STOCK)
        }

        val reservation = StockReservation.create(orderNumber, quantity, reservedAt, expiresAt)
        reservations.add(reservation)
        reservedQuantity += quantity

        return reservation
    }

    fun confirmReservation(
        orderNumber: String,
        confirmedAt: LocalDateTime,
    ) {
        val reservation = findReservation(orderNumber)
        reservation.confirm(confirmedAt)
        reservedQuantity -= reservation.quantity
        totalQuantity -= reservation.quantity
    }

    fun releaseReservation(
        orderNumber: String,
        releasedAt: LocalDateTime,
    ) {
        val reservation = findReservation(orderNumber)
        reservation.release(releasedAt)
        reservedQuantity -= reservation.quantity
    }

    fun expireReservation(
        orderNumber: String,
        expiredAt: LocalDateTime,
    ) {
        val reservation = findReservation(orderNumber)
        reservation.expire(expiredAt)
        reservedQuantity -= reservation.quantity
    }

    fun findReservation(orderNumber: String): StockReservation {
        return reservations.firstOrNull { it.orderNumber == orderNumber }
            ?: throw BusinessException(InventoryErrorCode.STOCK_RESERVATION_NOT_FOUND)
    }

    private fun requirePositiveQuantity(quantity: Long) {
        require(quantity > 0) { "변경 수량은 1개 이상이어야 합니다." }
    }
}
