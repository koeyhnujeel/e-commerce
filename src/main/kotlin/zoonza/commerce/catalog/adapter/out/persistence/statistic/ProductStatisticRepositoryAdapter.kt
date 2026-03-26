package zoonza.commerce.catalog.adapter.out.persistence.statistic

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.domain.statistic.ProductStatistic
import zoonza.commerce.catalog.domain.statistic.ProductStatisticRepository

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
        if (delta == 0L) {
            return
        }

        val updated = productStatisticJpaRepository.updateLikeCount(
            productId = productId,
            delta = delta,
        )

        if (updated > 0 || delta < 0L) {
            return
        }

        try {
            productStatisticJpaRepository.save(
                ProductStatistic.create(
                    productId = productId,
                    likeCount = delta,
                ),
            )
        } catch (_: DataIntegrityViolationException) {
            productStatisticJpaRepository.updateLikeCount(
                productId = productId,
                delta = delta,
            )
        }
    }
}
