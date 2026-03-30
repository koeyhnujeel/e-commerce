package zoonza.commerce.catalog.adapter.out.persistence.product.projection

import com.querydsl.core.annotations.QueryProjection
import zoonza.commerce.catalog.domain.product.ProductSaleStatus

@QueryProjection
data class ProductSummaryProjection(
    val productId: Long,
    val name: String,
    val brandName: String,
    val primaryImageUrl: String,
    val basePrice: Long,
    val likeCount: Long,
    val saleStatus: ProductSaleStatus,
)
