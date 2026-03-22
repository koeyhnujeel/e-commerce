package zoonza.commerce.catalog.application.dto

import zoonza.commerce.catalog.domain.ProductSaleStatus

data class ProductDetail(
    val productId: Long,
    val name: String,
    val description: String,
    val basePrice: Long,
    val categoryIds: List<Long>,
    val images: List<ProductImageDetail>,
    val options: List<ProductOptionDetail>,
    val likeCount: Long,
    val likedByMe: Boolean,
    val saleStatus: ProductSaleStatus,
)

data class ProductImageDetail(
    val imageUrl: String,
    val isPrimary: Boolean,
    val sortOrder: Int,
)

data class ProductOptionDetail(
    val productOptionId: Long,
    val color: String,
    val size: String,
    val stockId: Long,
    val orderable: Boolean,
)
