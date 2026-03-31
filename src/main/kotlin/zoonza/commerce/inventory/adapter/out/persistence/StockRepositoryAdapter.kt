package zoonza.commerce.inventory.adapter.out.persistence

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.inventory.domain.InventoryErrorCode
import zoonza.commerce.inventory.domain.Stock
import zoonza.commerce.inventory.domain.StockRepository
import zoonza.commerce.shared.BusinessException

@Repository
class StockRepositoryAdapter(
    private val stockJpaRepository: StockJpaRepository,
) : StockRepository {
    override fun findByProductOptionId(productOptionId: Long): Stock? {
        return stockJpaRepository.findByProductOptionId(productOptionId)?.toDomain()
    }

    override fun findAllByProductOptionIds(productOptionIds: Set<Long>): List<Stock> {
        if (productOptionIds.isEmpty()) {
            return emptyList()
        }

        return stockJpaRepository.findAllByProductOptionIdIn(productOptionIds).map(StockJpaEntity::toDomain)
    }

    override fun save(stock: Stock): Stock {
        val jpaEntity = if (stock.id == 0L) {
            StockJpaEntity.from(stock)
        } else {
            stockJpaRepository.findByIdOrNull(stock.id)
                ?.also { it.updateFrom(stock) }
                ?: throw BusinessException(InventoryErrorCode.STOCK_NOT_FOUND)
        }

        return stockJpaRepository.save(jpaEntity).toDomain()
    }
}
