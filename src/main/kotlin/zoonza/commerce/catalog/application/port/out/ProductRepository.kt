package zoonza.commerce.catalog.application.port.out

import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.domain.Product
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult

interface ProductRepository {
    fun existsById(id: Long): Boolean

    fun findAll(
        categoryId: Long?,
        pageQuery: PageQuery,
        sort: ProductListSort,
    ): PageResult<Product>

    fun findById(id: Long): Product?

    fun findPrimaryImageUrlsByProductIds(productIds: Collection<Long>): Map<Long, String>

    fun findByOptionId(productOptionId: Long): Product?
}
