package zoonza.commerce.catalog.adapter.`in`.response

import zoonza.commerce.catalog.domain.product.ProductSaleStatus

data class ProductSummaryResponse(
    val productId: Long,
    val name: String,
    val primaryImageUrl: String,
    val basePrice: Long,
    val likeCount: Long,
    val likedByMe: Boolean,
    val saleStatus: ProductSaleStatus,
)
