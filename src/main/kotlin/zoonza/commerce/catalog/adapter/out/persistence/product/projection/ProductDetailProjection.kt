package zoonza.commerce.catalog.adapter.out.persistence.product.projection

import com.querydsl.core.annotations.QueryProjection
import java.math.BigDecimal

@QueryProjection
data class ProductDetailProjection(
    val productId: Long,
    val name: String,
    val brandName: String,
    val description: String,
    val basePrice: BigDecimal,
    val categoryId: Long,
    val likeCount: Long,
)
