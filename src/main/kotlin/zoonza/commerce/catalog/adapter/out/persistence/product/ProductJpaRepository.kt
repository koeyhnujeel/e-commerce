package zoonza.commerce.catalog.adapter.out.persistence.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProductJpaRepository : JpaRepository<ProductEntity, Long> {
    @Query(
        """
        select distinct product
        from ProductEntity product
        join product.options option
        where option.id = :productOptionId
        """,
    )
    fun findByOptionId(
        @Param("productOptionId") productOptionId: Long,
    ): ProductEntity?
}
