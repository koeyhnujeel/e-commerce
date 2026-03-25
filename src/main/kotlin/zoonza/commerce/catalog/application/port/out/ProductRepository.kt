package zoonza.commerce.catalog.application.port.out

import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.domain.Product
import zoonza.commerce.catalog.domain.ProductSaleStatus
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult

interface ProductRepository {
    fun existsById(id: Long): Boolean

    fun findById(id: Long): Product?

    fun findByOptionId(productOptionId: Long): Product?

    fun findPageByCategoryIds(
        categoryIds: Set<Long>?,
        pageQuery: PageQuery,
        sort: ProductListSort,
    ): PageResult<ProductSummaryQueryResult>
}

data class ProductSummaryQueryResult(
    val productId: Long,
    val name: String,
    val primaryImageUrl: String,
    val basePrice: Long,
    val likeCount: Long,
    val saleStatus: ProductSaleStatus,
)
