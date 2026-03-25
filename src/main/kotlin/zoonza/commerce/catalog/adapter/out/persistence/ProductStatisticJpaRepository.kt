package zoonza.commerce.catalog.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import zoonza.commerce.catalog.domain.ProductStatistic

interface ProductStatisticJpaRepository : JpaRepository<ProductStatistic, Long> {
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update ProductStatistic statistic
        set statistic.likeCount =
            case
                when statistic.likeCount + :delta < 0 then 0
                else statistic.likeCount + :delta
            end
        where statistic.productId = :productId
        """,
    )
    fun updateLikeCount(
        @Param("productId") productId: Long,
        @Param("delta") delta: Long,
    ): Int
}
