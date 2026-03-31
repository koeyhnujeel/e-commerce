package zoonza.commerce.inventory.domain

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
            require(orderNumber.isNotBlank()) { "주문번호는 비어 있을 수 없습니다." }
            require(quantity > 0) { "예약 수량은 1개 이상이어야 합니다." }
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
        require(status == StockReservationStatus.RESERVED) { "예약 상태가 아니면 확정할 수 없습니다." }
        require(!confirmedAt.isBefore(reservedAt)) { "확정 시각은 예약 시각 이후여야 합니다." }
        require(!confirmedAt.isAfter(expiresAt)) { "만료된 예약은 확정할 수 없습니다." }

        this.status = StockReservationStatus.CONFIRMED
        this.confirmedAt = confirmedAt
    }

    fun release(releasedAt: LocalDateTime) {
        require(status == StockReservationStatus.RESERVED) { "예약 상태가 아니면 해제할 수 없습니다." }
        require(!releasedAt.isBefore(reservedAt)) { "해제 시각은 예약 시각 이후여야 합니다." }

        this.status = StockReservationStatus.RELEASED
        this.releasedAt = releasedAt
    }

    fun expire(expiredAt: LocalDateTime) {
        require(status == StockReservationStatus.RESERVED) { "예약 상태가 아니면 만료 처리할 수 없습니다." }
        require(!expiredAt.isBefore(expiresAt)) { "만료 시각은 예약 만료 시간 이후여야 합니다." }

        this.status = StockReservationStatus.EXPIRED
        this.expiredAt = expiredAt
    }
}
