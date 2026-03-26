package zoonza.commerce.catalog.adapter.`in`.response

import zoonza.commerce.catalog.application.dto.ProductSummary
import zoonza.commerce.catalog.domain.product.ProductSaleStatus

data class ProductSummaryResponse(
    val productId: Long,
    val name: String,
    val primaryImageUrl: String,
    val basePrice: Long,
    val likeCount: Long,
    val likedByMe: Boolean,
    val saleStatus: ProductSaleStatus,
) {
    companion object {
        fun from(product: ProductSummary): ProductSummaryResponse {
            return ProductSummaryResponse(
                productId = product.productId,
                name = product.name,
                primaryImageUrl = product.primaryImageUrl,
                basePrice = product.basePrice,
                likeCount = product.likeCount,
                likedByMe = product.likedByMe,
                saleStatus = product.saleStatus,
            )
        }
    }
}
