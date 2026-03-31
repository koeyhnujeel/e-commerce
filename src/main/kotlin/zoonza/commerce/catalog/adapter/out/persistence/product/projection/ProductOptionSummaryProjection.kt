package zoonza.commerce.catalog.adapter.out.persistence.product.projection

import com.querydsl.core.annotations.QueryProjection
import zoonza.commerce.catalog.domain.product.ProductSaleStatus
import java.math.BigDecimal

@QueryProjection
data class ProductOptionSummaryProjection(
    val productId: Long,
    val productOptionId: Long,
    val productName: String,
    val basePrice: BigDecimal,
    val additionalPrice: BigDecimal,
    val color: String,
    val size: String,
    val saleStatus: ProductSaleStatus,
)
