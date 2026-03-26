package zoonza.commerce.catalog.adapter.out.persistence.product.projection

import com.querydsl.core.annotations.QueryProjection

@QueryProjection
data class ProductImageDetailProjection(
    val imageUrl: String,
    val isPrimary: Boolean,
    val sortOrder: Int,
)
