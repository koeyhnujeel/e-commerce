package zoonza.commerce.catalog.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import zoonza.commerce.catalog.domain.Product

interface ProductJpaRepository : JpaRepository<Product, Long>, ProductQueryRepository {
    @Query(
        """
        select distinct product
        from Product product
        join product.options option
        where option.id = :productOptionId
        """,
    )
    fun findByOptionId(
        @Param("productOptionId") productOptionId: Long,
    ): Product?
}
