package zoonza.commerce.catalog.adapter.out.persistence.statistic

import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.domain.statistic.ProductStatistic
import zoonza.commerce.catalog.domain.statistic.ProductStatisticRepository

@Repository
class ProductStatisticRepositoryAdapter(
    private val productStatisticJpaRepository: ProductStatisticJpaRepository,
) : ProductStatisticRepository {
    override fun findByProductId(productId: Long): ProductStatistic? {
        return productStatisticJpaRepository.findByProductId(productId)?.toDomain()
    }

    override fun save(productStatistic: ProductStatistic): ProductStatistic {
        return productStatisticJpaRepository.save(
            ProductStatisticJpaEntity.from(productStatistic)
        ).toDomain()
    }
}
