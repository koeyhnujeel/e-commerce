package zoonza.commerce.catalog.adapter.out.persistence.product.projection

import com.querydsl.core.annotations.QueryProjection
import zoonza.commerce.catalog.domain.product.ProductSaleStatus
import java.math.BigDecimal

@QueryProjection
data class ProductSummaryProjection(
    val productId: Long,
    val name: String,
    val brandName: String,
    val primaryImageUrl: String,
    val basePrice: BigDecimal,
    val likeCount: Long,
    val saleStatus: ProductSaleStatus,
)
