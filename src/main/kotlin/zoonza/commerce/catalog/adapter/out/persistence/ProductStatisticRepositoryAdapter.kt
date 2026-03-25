package zoonza.commerce.catalog.adapter.out.persistence

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.application.port.out.ProductStatisticRepository
import zoonza.commerce.catalog.domain.ProductStatistic

@Repository
class ProductStatisticRepositoryAdapter(
    private val productStatisticJpaRepository: ProductStatisticJpaRepository,
) : ProductStatisticRepository {
    override fun findLikeCount(productId: Long): Long {
        return productStatisticJpaRepository.findByIdOrNull(productId)?.likeCount ?: 0L
    }

    override fun applyLikeCountDelta(
        productId: Long,
        delta: Long,
    ) {
        val statistic = productStatisticJpaRepository.findByIdOrNull(productId)

        if (statistic == null) {
            if (delta <= 0L) {
                return
            }

            productStatisticJpaRepository.save(
                ProductStatistic.create(
                    productId = productId,
                    likeCount = delta,
                ),
            )
            return
        }

        statistic.applyLikeCountDelta(delta)
        productStatisticJpaRepository.save(statistic)
    }
}
