package zoonza.commerce.catalog.adapter.out.persistence

import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.port.out.ProductSummaryQueryResult
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult

interface ProductQueryRepository {
    fun findPageByCategoryIds(
        categoryIds: Set<Long>,
        pageQuery: PageQuery,
        sort: ProductListSort,
    ): PageResult<ProductSummaryQueryResult>
}
