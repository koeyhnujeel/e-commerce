package zoonza.commerce.inventory.adapter.out.persistence

import jakarta.persistence.*
import zoonza.commerce.inventory.domain.Stock

@Entity
@Table(
    name = "stock",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_stock_product_option_id",
            columnNames = ["product_option_id"],
        ),
    ],
)
class StockJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "product_option_id", nullable = false)
    val productOptionId: Long = 0,

    @Column(name = "total_quantity", nullable = false)
    var totalQuantity: Long = 0,

    @Column(name = "reserved_quantity", nullable = false)
    var reservedQuantity: Long = 0,

    @Version
    @Column(name = "version", nullable = false)
    val version: Long? = null,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "stock_id", nullable = false)
    val reservations: MutableList<StockReservationJpaEntity> = mutableListOf(),
) {
    companion object {
        fun from(stock: Stock): StockJpaEntity {
            return StockJpaEntity(
                id = stock.id,
                productOptionId = stock.productOptionId,
                totalQuantity = stock.totalQuantity,
                reservedQuantity = stock.reservedQuantity,
                reservations = stock.reservations.map(StockReservationJpaEntity::from).toMutableList(),
            )
        }
    }

    fun toDomain(): Stock {
        return Stock(
            id = id,
            productOptionId = productOptionId,
            totalQuantity = totalQuantity,
            reservedQuantity = reservedQuantity,
            reservations = reservations.map(StockReservationJpaEntity::toDomain).toMutableList(),
        )
    }

    fun updateFrom(stock: Stock) {
        totalQuantity = stock.totalQuantity
        reservedQuantity = stock.reservedQuantity
        reservations.clear()
        reservations.addAll(stock.reservations.map(StockReservationJpaEntity::from))
    }
}
