package zoonza.commerce.catalog.adapter.`in`.response

import zoonza.commerce.catalog.application.dto.ProductDetail
import zoonza.commerce.catalog.application.dto.ProductImageDetail
import zoonza.commerce.catalog.application.dto.ProductOptionDetail
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
    val saleStatus: ProductSaleStatus,
) {
    companion object {
        fun from(product: ProductDetail): ProductDetailResponse {
            return ProductDetailResponse(
                productId = product.productId,
                name = product.name,
                description = product.description,
                basePrice = product.basePrice,
                categoryId = product.categoryId,
                images = product.images.map(ProductImageResponse::from),
                options = product.options.map(ProductOptionResponse::from),
                likeCount = product.likeCount,
                saleStatus = product.saleStatus,
            )
        }
    }
}

data class ProductImageResponse(
    val imageUrl: String,
    val isPrimary: Boolean,
    val sortOrder: Int,
) {
    companion object {
        fun from(image: ProductImageDetail): ProductImageResponse {
            return ProductImageResponse(
                imageUrl = image.imageUrl,
                isPrimary = image.isPrimary,
                sortOrder = image.sortOrder,
            )
        }
    }
}

data class ProductOptionResponse(
    val productOptionId: Long,
    val color: String,
    val size: String,
    val sortOrder: Int,
    val additionalPrice: Long,
) {
    companion object {
        fun from(option: ProductOptionDetail): ProductOptionResponse {
            return ProductOptionResponse(
                productOptionId = option.productOptionId,
                color = option.color,
                size = option.size,
                sortOrder = option.sortOrder,
                additionalPrice = option.additionalPrice,
            )
        }
    }
}
