package zoonza.commerce.catalog.application.port.`in`

import zoonza.commerce.catalog.application.dto.ProductDetail
import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.application.dto.ProductSummary
import zoonza.commerce.support.pagination.PageResponse

interface CatalogService {
    fun getProductsByCategory(
        memberId: Long?,
        page: Int,
        size: Int,
        categoryId: Long,
        sort: ProductListSort,
    ): PageResponse<ProductSummary>

    fun getProduct(
        productId: Long,
        memberId: Long?,
    ): ProductDetail
}
