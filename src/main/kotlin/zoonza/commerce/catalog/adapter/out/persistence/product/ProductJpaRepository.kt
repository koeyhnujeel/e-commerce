package zoonza.commerce.catalog.adapter.out.persistence.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProductJpaRepository : JpaRepository<ProductJpaEntity, Long> {
    @Query(
        """
        select distinct product
        from ProductJpaEntity product
        join product.options option
        where option.id = :productOptionId
        """,
    )
    fun findByOptionId(
        @Param("productOptionId") productOptionId: Long,
    ): ProductJpaEntity?
}
