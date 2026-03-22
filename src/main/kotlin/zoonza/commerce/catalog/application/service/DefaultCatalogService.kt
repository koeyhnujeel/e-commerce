package zoonza.commerce.catalog.application.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.catalog.ProductOptionSnapshot
import zoonza.commerce.catalog.adapter.out.persistence.ProductJpaRepository
import zoonza.commerce.catalog.adapter.out.persistence.ProductOptionJpaRepository
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.ErrorCode

@Service
class DefaultCatalogService(
    private val productJpaRepository: ProductJpaRepository,
    private val productOptionJpaRepository: ProductOptionJpaRepository,
) : CatalogApi {
    override fun existsProduct(id: Long): Boolean {
        return productJpaRepository.existsById(id)
    }

    override fun findProductOptionSnapshot(productOptionId: Long): ProductOptionSnapshot {
        val option = productOptionJpaRepository.findByIdOrNull(productOptionId)
            ?: throw BusinessException(ErrorCode.PRODUCT_OPTION_NOT_FOUND)

        return ProductOptionSnapshot(
            color = option.color,
            size = option.size,
        )
    }
}
