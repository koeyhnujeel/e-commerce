package zoonza.commerce.catalog.domain.statistic

interface ProductStatisticRepository {
    fun findByProductId(productId: Long): ProductStatistic?

    fun save(productStatistic: ProductStatistic): ProductStatistic
}