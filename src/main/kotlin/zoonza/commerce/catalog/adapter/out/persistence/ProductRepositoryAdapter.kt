package zoonza.commerce.catalog.adapter.out.persistence

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.application.port.out.ProductRepository
import zoonza.commerce.catalog.domain.Product
import zoonza.commerce.catalog.domain.QProduct.Companion.product
import zoonza.commerce.catalog.domain.QProductOption.Companion.productOption

@Repository
class ProductRepositoryAdapter(
    private val queryFactory: JPAQueryFactory,
    private val productJpaRepository: ProductJpaRepository,
) : ProductRepository {
    override fun existsById(id: Long): Boolean {
        return productJpaRepository.existsById(id)
    }

    override fun findById(id: Long): Product? {
        return productJpaRepository.findByIdOrNull(id)
    }

    override fun findByOptionId(productOptionId: Long): Product? {
        return queryFactory
            .selectDistinct(product)
            .from(product)
            .join(product.options, productOption)
            .where(productOption.id.eq(productOptionId))
            .fetchOne()
    }
}
