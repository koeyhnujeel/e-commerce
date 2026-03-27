package zoonza.commerce.catalog.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.CatalogErrorCode
import zoonza.commerce.catalog.domain.product.ProductRepository
import zoonza.commerce.catalog.domain.statistic.ProductStatistic
import zoonza.commerce.catalog.domain.statistic.ProductStatisticErrorCode
import zoonza.commerce.catalog.domain.statistic.ProductStatisticRepository
import zoonza.commerce.shared.BusinessException

@Service
class DefaultProductStatisticService(
    private val productRepository: ProductRepository,
    private val productStatisticRepository: ProductStatisticRepository
) {
    @Transactional
    fun incrementProductLikeCount(productId: Long) {
        validateProductExists(productId)

        val stat = productStatisticRepository.findByProductIdOrThrow(productId)

        stat.incrementLikeCount()

        productStatisticRepository.save(stat)
    }

    @Transactional
    fun decrementProductLikeCount(productId: Long) {
        validateProductExists(productId)

        val stat = productStatisticRepository.findByProductIdOrThrow(productId)

        stat.decrementLikeCount()

        productStatisticRepository.save(stat)
    }

    private fun validateProductExists(productId: Long) {
        if (!productRepository.existsById(productId)) {
            throw BusinessException(CatalogErrorCode.PRODUCT_NOT_FOUND)
        }
    }

    private fun ProductStatisticRepository.findByProductIdOrThrow(productId: Long): ProductStatistic {
        return this.findByProductId(productId)
            ?: throw BusinessException(ProductStatisticErrorCode.PRODUCT_STATISTIC_NOT_FOUND)
    }
}