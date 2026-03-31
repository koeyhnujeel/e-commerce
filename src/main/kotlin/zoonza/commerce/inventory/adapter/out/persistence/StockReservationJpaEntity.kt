package zoonza.commerce.inventory.adapter.out.persistence

import jakarta.persistence.*
import zoonza.commerce.inventory.domain.StockReservation
import zoonza.commerce.inventory.domain.StockReservationStatus
import java.time.LocalDateTime

@Entity
@Table(
    name = "stock_reservation",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_stock_reservation_stock_id_order_number",
            columnNames = ["stock_id", "order_number"],
        ),
    ],
)
class StockReservationJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "order_number", nullable = false)
    val orderNumber: String = "",

    @Column(nullable = false)
    val quantity: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: StockReservationStatus = StockReservationStatus.RESERVED,

    @Column(name = "reserved_at", nullable = false)
    val reservedAt: LocalDateTime = LocalDateTime.MIN,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime = LocalDateTime.MIN,

    @Column(name = "confirmed_at")
    val confirmedAt: LocalDateTime? = null,

    @Column(name = "released_at")
    val releasedAt: LocalDateTime? = null,

    @Column(name = "expired_at")
    val expiredAt: LocalDateTime? = null,
) {
    companion object {
        fun from(stockReservation: StockReservation): StockReservationJpaEntity {
            return StockReservationJpaEntity(
                id = stockReservation.id,
                orderNumber = stockReservation.orderNumber,
                quantity = stockReservation.quantity,
                status = stockReservation.status,
                reservedAt = stockReservation.reservedAt,
                expiresAt = stockReservation.expiresAt,
                confirmedAt = stockReservation.confirmedAt,
                releasedAt = stockReservation.releasedAt,
                expiredAt = stockReservation.expiredAt,
            )
        }
    }

    fun toDomain(): StockReservation {
        return StockReservation(
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
