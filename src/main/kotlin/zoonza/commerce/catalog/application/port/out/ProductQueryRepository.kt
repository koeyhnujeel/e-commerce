package zoonza.commerce.catalog.application.port.out

import zoonza.commerce.catalog.application.dto.ProductListSort
import zoonza.commerce.catalog.domain.product.ProductSaleStatus
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult

interface ProductQueryRepository {
    fun findProductDetailsById(id: Long): ProductDetailQueryResult?

    fun findPageByCategoryIds(
        categoryIds: Set<Long>,
        pageQuery: PageQuery,
        sort: ProductListSort
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

data class ProductDetailQueryResult(
    val productId: Long,
    val name: String,
    val description: String,
    val basePrice: Long,
    val categoryId: Long,
    val images: List<ProductImageQueryResult>,
    val options: List<ProductOptionQueryResult>,
    val likeCount: Long,
)

data class ProductImageQueryResult(
    val imageUrl: String,
    val isPrimary: Boolean,
    val sortOrder: Int,
)

data class ProductOptionQueryResult(
    val productOptionId: Long,
    val color: String,
    val size: String,
    val sortOrder: Int,
    val additionalPrice: Long,
)