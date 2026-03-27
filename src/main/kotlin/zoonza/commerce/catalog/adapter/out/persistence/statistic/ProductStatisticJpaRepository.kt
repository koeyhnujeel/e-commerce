package zoonza.commerce.catalog.adapter.out.persistence.statistic

import org.springframework.data.jpa.repository.JpaRepository

interface ProductStatisticJpaRepository : JpaRepository<ProductStatisticJpaEntity, Long> {
    fun findByProductId(productId: Long): ProductStatisticJpaEntity?
}
