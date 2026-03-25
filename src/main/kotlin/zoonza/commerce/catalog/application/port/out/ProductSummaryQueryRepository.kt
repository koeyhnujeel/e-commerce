package zoonza.commerce.catalog.application.port.out

import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.domain.ProductSaleStatus
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult

interface ProductSummaryQueryRepository {
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
