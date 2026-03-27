package zoonza.commerce.like.adapter.`in`.response

import zoonza.commerce.like.application.dto.ProductLikeStatus

data class ProductLikeStatusResponse(
    val productId: Long,
    val liked: Boolean,
) {
    companion object {
        fun from(productLikeStatus: ProductLikeStatus): ProductLikeStatusResponse {
            return ProductLikeStatusResponse(
                productId = productLikeStatus.productId,
                liked = productLikeStatus.liked,
            )
        }
    }
}
