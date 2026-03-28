package zoonza.commerce.catalog.adapter.out.persistence.statistic

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param


interface ProductStatisticJpaRepository : JpaRepository<ProductStatisticJpaEntity, Long> {
    fun findByProductId(productId: Long): ProductStatisticJpaEntity?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE ProductStatisticJpaEntity statistic
        SET statistic.likeCount = statistic.likeCount + 1
        WHERE statistic.productId = :productId
        """,
    )
    fun incrementLikeCount(@Param("productId") productId: Long): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE ProductStatisticJpaEntity statistic
        SET statistic.likeCount = statistic.likeCount - 1
        WHERE statistic.productId = :productId
          AND statistic.likeCount > 0
        """,
    )
    fun decrementLikeCount(@Param("productId") productId: Long): Int
}
