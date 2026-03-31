package zoonza.commerce.inventory

import java.time.LocalDateTime

interface InventoryApi {
    fun createStock(
        productOptionId: Long,
        totalQuantity: Long,
    ): Long

    fun getStock(productOptionId: Long): StockSnapshot

    fun getAvailableQuantities(productOptionIds: Set<Long>): Map<Long, Long>

    fun reserve(
        productOptionId: Long,
        orderNumber: String,
        quantity: Long,
        reservedAt: LocalDateTime,
        expiresAt: LocalDateTime,
    ): StockReservationSnapshot

    fun confirmReservation(
        productOptionId: Long,
        orderNumber: String,
        confirmedAt: LocalDateTime,
    )

    fun releaseReservation(
        productOptionId: Long,
        orderNumber: String,
        releasedAt: LocalDateTime,
    )

    fun expireReservation(
        productOptionId: Long,
        orderNumber: String,
        expiredAt: LocalDateTime,
    )

    fun increaseStock(
        productOptionId: Long,
        quantity: Long,
    )

    fun decreaseStock(
        productOptionId: Long,
        quantity: Long,
    )
}
