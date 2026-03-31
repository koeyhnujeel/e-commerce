package zoonza.commerce.inventory.domain

import zoonza.commerce.shared.BusinessException
import java.time.LocalDateTime

class StockReservation(
    val id: Long = 0,
    val orderNumber: String,
    val quantity: Long,
    var status: StockReservationStatus,
    val reservedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    var confirmedAt: LocalDateTime? = null,
    var releasedAt: LocalDateTime? = null,
    var expiredAt: LocalDateTime? = null,
) {
    companion object {
        fun create(
            orderNumber: String,
            quantity: Long,
            reservedAt: LocalDateTime,
            expiresAt: LocalDateTime,
        ): StockReservation {
            if (orderNumber.isBlank()) {
                throw BusinessException(InventoryErrorCode.INVALID_STOCK_RESERVATION_STATUS)
            }

            if (quantity <= 0) {
                throw BusinessException(InventoryErrorCode.INVALID_STOCK_QUANTITY)
            }

            require(expiresAt.isAfter(reservedAt)) { "만료 시각은 예약 시각 이후여야 합니다." }

            return StockReservation(
                orderNumber = orderNumber,
                quantity = quantity,
                status = StockReservationStatus.RESERVED,
                reservedAt = reservedAt,
                expiresAt = expiresAt,
            )
        }
    }

    fun confirm(confirmedAt: LocalDateTime) {
        assertReserved()
        require(!confirmedAt.isBefore(reservedAt)) { "확정 시각은 예약 시각 이후여야 합니다." }
        require(!confirmedAt.isAfter(expiresAt)) { "만료된 예약은 확정할 수 없습니다." }

        this.status = StockReservationStatus.CONFIRMED
        this.confirmedAt = confirmedAt
    }

    fun release(releasedAt: LocalDateTime) {
        assertReserved()
        require(!releasedAt.isBefore(reservedAt)) { "해제 시각은 예약 시각 이후여야 합니다." }

        this.status = StockReservationStatus.RELEASED
        this.releasedAt = releasedAt
    }

    fun expire(expiredAt: LocalDateTime) {
        assertReserved()
        require(!expiredAt.isBefore(expiresAt)) { "만료 시각은 예약 만료 시간 이후여야 합니다." }

        this.status = StockReservationStatus.EXPIRED
        this.expiredAt = expiredAt
    }

    private fun assertReserved() {
        if (status != StockReservationStatus.RESERVED) {
            throw BusinessException(InventoryErrorCode.INVALID_STOCK_RESERVATION_STATUS)
        }
    }
}
