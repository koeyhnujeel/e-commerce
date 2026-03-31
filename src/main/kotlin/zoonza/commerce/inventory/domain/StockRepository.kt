package zoonza.commerce.inventory.domain

interface StockRepository {
    fun findByProductOptionId(productOptionId: Long): Stock?

    fun findAllByProductOptionIds(productOptionIds: Set<Long>): List<Stock>

    fun save(stock: Stock): Stock
}
