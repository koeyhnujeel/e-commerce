package zoonza.commerce.catalog.domain.statistic

interface ProductStatisticRepository {
    fun findLikeCount(productId: Long): Long

    fun applyLikeCountDelta(
        productId: Long,
        delta: Long,
    )
}