package zoonza.commerce.catalog.application.dto

import zoonza.commerce.catalog.domain.product.ProductSaleStatus

data class ProductSummary(
    val productId: Long,
    val name: String,
    val primaryImageUrl: String,
    val basePrice: Long,
    val likeCount: Long,
    val saleStatus: ProductSaleStatus,
)
