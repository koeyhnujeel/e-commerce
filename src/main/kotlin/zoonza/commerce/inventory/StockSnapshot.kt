package zoonza.commerce.inventory

import zoonza.commerce.inventory.domain.Stock

data class StockSnapshot(
    val id: Long,
    val productOptionId: Long,
    val totalQuantity: Long,
    val reservedQuantity: Long,
    val availableQuantity: Long,
) {
    companion object {
        fun from(stock: Stock): StockSnapshot {
            return StockSnapshot(
                id = stock.id,
                productOptionId = stock.productOptionId,
                totalQuantity = stock.totalQuantity,
                reservedQuantity = stock.reservedQuantity,
                availableQuantity = stock.availableQuantity(),
            )
        }
    }
}
