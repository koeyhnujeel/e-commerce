package zoonza.commerce.catalog.application.service

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.catalog.domain.product.ProductErrorCode
import zoonza.commerce.catalog.domain.product.ProductRepository
import zoonza.commerce.shared.BusinessException

@Component
class CatalogApiImpl(
    private val productRepository: ProductRepository,
) : CatalogApi {

    @Transactional(readOnly = true)
    override fun validateProductExists(id: Long) {
        if (!productRepository.existsById(id)) {
            throw BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND)
        }
    }
}
