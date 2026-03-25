package zoonza.commerce.catalog.application.port.out

interface ProductStatisticRepository {
    fun findLikeCount(productId: Long): Long

    fun applyLikeCountDelta(
        productId: Long,
        delta: Long,
    )
}
