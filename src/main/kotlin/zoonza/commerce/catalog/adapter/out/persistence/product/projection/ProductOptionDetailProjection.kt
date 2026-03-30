package zoonza.commerce.catalog.adapter.out.persistence.product.projection

import com.querydsl.core.annotations.QueryProjection
import java.math.BigDecimal

@QueryProjection
data class ProductOptionDetailProjection(
    val productOptionId: Long,
    val color: String,
    val size: String,
    val sortOrder: Int,
    val additionalPrice: BigDecimal,
)
