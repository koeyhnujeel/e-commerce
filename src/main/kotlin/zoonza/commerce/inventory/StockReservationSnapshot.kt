package zoonza.commerce.inventory

import zoonza.commerce.inventory.domain.StockReservation
import zoonza.commerce.inventory.domain.StockReservationStatus
import java.time.LocalDateTime

data class StockReservationSnapshot(
    val id: Long,
    val productOptionId: Long,
    val orderNumber: String,
    val quantity: Long,
    val status: StockReservationStatus,
    val reservedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val confirmedAt: LocalDateTime?,
    val releasedAt: LocalDateTime?,
    val expiredAt: LocalDateTime?,
) {
    companion object {
        fun from(
            productOptionId: Long,
            reservation: StockReservation,
        ): StockReservationSnapshot {
            return StockReservationSnapshot(
                id = reservation.id,
                productOptionId = productOptionId,
                orderNumber = reservation.orderNumber,
                quantity = reservation.quantity,
                status = reservation.status,
                reservedAt = reservation.reservedAt,
                expiresAt = reservation.expiresAt,
                confirmedAt = reservation.confirmedAt,
                releasedAt = reservation.releasedAt,
                expiredAt = reservation.expiredAt,
            )
        }
    }
}
