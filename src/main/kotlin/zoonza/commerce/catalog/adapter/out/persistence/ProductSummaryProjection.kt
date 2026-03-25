package zoonza.commerce.catalog.adapter.out.persistence

import com.querydsl.core.annotations.QueryProjection
import zoonza.commerce.catalog.domain.ProductSaleStatus

data class ProductSummaryProjection
    @QueryProjection
    constructor(
        val productId: Long,
        val name: String,
        val primaryImageUrl: String,
        val basePrice: Long,
        val likeCount: Long,
        val saleStatus: ProductSaleStatus,
    )
