package zoonza.commerce.catalog.adapter.out.persistence.product.projection

import com.querydsl.core.annotations.QueryProjection

@QueryProjection
data class ProductDetailProjection(
    val productId: Long,
    val name: String,
    val description: String,
    val basePrice: Long,
    val categoryId: Long,
    val likeCount: Long,
)
