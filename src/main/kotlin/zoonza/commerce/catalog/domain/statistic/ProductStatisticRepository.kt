package zoonza.commerce.catalog.domain.statistic

interface ProductStatisticRepository {
    fun findByProductId(productId: Long): ProductStatistic?

    fun save(productStatistic: ProductStatistic): ProductStatistic

    fun incrementLikeCount(productId: Long): Int

    fun decrementLikeCount(productId: Long): Int
}
