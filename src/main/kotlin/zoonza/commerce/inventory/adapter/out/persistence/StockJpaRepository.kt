package zoonza.commerce.inventory.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface StockJpaRepository : JpaRepository<StockJpaEntity, Long> {
    fun findByProductOptionId(productOptionId: Long): StockJpaEntity?
}
