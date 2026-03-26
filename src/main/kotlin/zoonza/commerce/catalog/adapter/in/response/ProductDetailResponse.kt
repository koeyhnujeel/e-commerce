package zoonza.commerce.catalog.adapter.`in`.response

import zoonza.commerce.catalog.domain.product.ProductSaleStatus

data class ProductDetailResponse(
    val productId: Long,
    val name: String,
    val description: String,
    val basePrice: Long,
    val categoryId: Long,
    val images: List<ProductImageResponse>,
    val options: List<ProductOptionResponse>,
    val likeCount: Long,
    val likedByMe: Boolean,
    val saleStatus: ProductSaleStatus,
)

data class ProductImageResponse(
    val imageUrl: String,
    val isPrimary: Boolean,
    val sortOrder: Int,
)

data class ProductOptionResponse(
    val productOptionId: Long,
    val color: String,
    val size: String,
    val sortOrder: Int,
    val additionalPrice: Long,
)
