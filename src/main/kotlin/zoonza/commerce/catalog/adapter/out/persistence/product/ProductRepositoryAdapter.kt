package zoonza.commerce.catalog.adapter.out.persistence.product

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.domain.product.Product
import zoonza.commerce.catalog.domain.product.ProductRepository

@Repository
class ProductRepositoryAdapter(
    private val productJpaRepository: ProductJpaRepository,
) : ProductRepository {
    override fun existsById(id: Long): Boolean {
        return productJpaRepository.existsById(id)
    }

    override fun findById(id: Long): Product? {
        return productJpaRepository.findByIdOrNull(id)?.let(ProductMapper::toDomain)
    }

    override fun findByOptionId(productOptionId: Long): Product? {
        return productJpaRepository.findByOptionId(productOptionId)?.let(ProductMapper::toDomain)
    }
}
