package zoonza.commerce.catalog.adapter.out.persistence

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.port.out.ProductRepository
import zoonza.commerce.catalog.application.port.out.ProductSummaryQueryResult
import zoonza.commerce.catalog.domain.Product
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult

@Repository
class ProductRepositoryAdapter(
    private val productJpaRepository: ProductJpaRepository,
) : ProductRepository {
    override fun existsById(id: Long): Boolean {
        return productJpaRepository.existsById(id)
    }

    override fun findById(id: Long): Product? {
        return productJpaRepository.findByIdOrNull(id)
    }

    override fun findByOptionId(productOptionId: Long): Product? {
        return productJpaRepository.findByOptionId(productOptionId)
    }

    override fun findPageByCategoryIds(
        categoryIds: Set<Long>,
        pageQuery: PageQuery,
        sort: ProductListSort,
    ): PageResult<ProductSummaryQueryResult> {
        return productJpaRepository.findPageByCategoryIds(
            categoryIds = categoryIds,
            pageQuery = pageQuery,
            sort = sort,
        )
    }
}
