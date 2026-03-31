package zoonza.commerce.inventory.domain

interface StockRepository {
    fun findByProductOptionId(productOptionId: Long): Stock?

    fun save(stock: Stock): Stock
}