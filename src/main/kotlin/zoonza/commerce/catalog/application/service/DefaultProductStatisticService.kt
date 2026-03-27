package zoonza.commerce.catalog.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.domain.statistic.ProductStatisticErrorCode
import zoonza.commerce.catalog.domain.statistic.ProductStatisticRepository
import zoonza.commerce.shared.BusinessException

@Service
class DefaultProductStatisticService(
    private val productStatisticRepository: ProductStatisticRepository
) {
    @Transactional
    fun incrementLikeCount(productId: Long) {
        val stat = productStatisticRepository.findByProductId(productId)
            ?: throw BusinessException(ProductStatisticErrorCode.PRODUCT_STATISTIC_NOT_FOUND)

        stat.incrementLikeCount()

        productStatisticRepository.save(stat)
    }

    @Transactional
    fun decrementLikeCount(productId: Long) {
        val stat = productStatisticRepository.findByProductId(productId)
            ?: throw BusinessException(ProductStatisticErrorCode.PRODUCT_STATISTIC_NOT_FOUND)

        stat.decrementLikeCount()

        productStatisticRepository.save(stat)
    }
}