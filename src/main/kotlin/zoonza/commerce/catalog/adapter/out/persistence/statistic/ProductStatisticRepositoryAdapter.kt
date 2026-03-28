package zoonza.commerce.catalog.adapter.out.persistence.statistic

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.domain.statistic.ProductStatistic
import zoonza.commerce.catalog.domain.statistic.ProductStatisticErrorCode
import zoonza.commerce.catalog.domain.statistic.ProductStatisticRepository
import zoonza.commerce.shared.BusinessException

@Repository
class ProductStatisticRepositoryAdapter(
    private val productStatisticJpaRepository: ProductStatisticJpaRepository,
) : ProductStatisticRepository {
    override fun findByProductId(productId: Long): ProductStatistic? {
        return productStatisticJpaRepository.findByProductId(productId)?.toDomain()
    }

    override fun save(productStatistic: ProductStatistic): ProductStatistic {
        val jpaEntity = if (productStatistic.id == 0L) {
            ProductStatisticJpaEntity.from(productStatistic)
        } else {
            productStatisticJpaRepository.findByIdOrNull(productStatistic.id)
                ?.also { it.updateFrom(productStatistic) }
                ?: throw BusinessException(ProductStatisticErrorCode.PRODUCT_STATISTIC_NOT_FOUND)
        }

        return productStatisticJpaRepository.save(jpaEntity).toDomain()
    }

    override fun incrementLikeCount(productId: Long): Int {
        return productStatisticJpaRepository.incrementLikeCount(productId)
    }

    override fun decrementLikeCount(productId: Long): Int {
        return productStatisticJpaRepository.decrementLikeCount(productId)
    }
}
