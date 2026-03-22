package zoonza.commerce.catalog.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import zoonza.commerce.catalog.domain.ProductOption

interface ProductOptionJpaRepository : JpaRepository<ProductOption, Long> {
    @Query(
        """
        select po
        from ProductOption po
        where po.product.id = :productId
        order by po.id asc
        """,
    )
    fun findAllByProductId(productId: Long): List<ProductOption>

    fun findByIdAndProductId(
        id: Long,
        productId: Long,
    ): ProductOption?
}
