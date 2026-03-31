package zoonza.commerce.catalog.application.service

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.catalog.ProductOptionSummary
import zoonza.commerce.catalog.application.port.out.ProductQueryRepository
import zoonza.commerce.catalog.domain.product.ProductErrorCode
import zoonza.commerce.catalog.domain.product.ProductRepository
import zoonza.commerce.catalog.domain.product.ProductSaleStatus
import zoonza.commerce.shared.BusinessException

@Component
class CatalogApiImpl(
    private val productRepository: ProductRepository,
    private val productQueryRepository: ProductQueryRepository,
) : CatalogApi {

    @Transactional(readOnly = true)
    override fun validateProductExists(id: Long) {
        if (!productRepository.existsById(id)) {
            throw BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND)
        }
    }

    @Transactional(readOnly = true)
    override fun validateAvailableProductOption(
        productId: Long,
        productOptionId: Long,
    ) {
        val product = productRepository.findById(productId)
            ?: throw BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND)

        product.validateAvailableOption(productOptionId)
    }

    @Transactional(readOnly = true)
    override fun getProductOptionSummaries(optionIds: Set<Long>): Map<Long, ProductOptionSummary> {
        return productQueryRepository.findProductOptionSummariesByOptionIds(optionIds)
            .associate { summary ->
                summary.productOptionId to ProductOptionSummary(
                    productId = summary.productId,
                    productOptionId = summary.productOptionId,
                    productName = summary.productName,
                    primaryImageUrl = summary.primaryImageUrl,
                    basePrice = summary.basePrice,
                    additionalPrice = summary.additionalPrice,
                    color = summary.color,
                    size = summary.size,
                    availableForSale = summary.saleStatus == ProductSaleStatus.AVAILABLE,
                )
            }
    }
}
