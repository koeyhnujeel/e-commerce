package zoonza.commerce.catalog.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import zoonza.commerce.catalog.domain.ProductImage

interface ProductImageJpaRepository : JpaRepository<ProductImage, Long> {
    @Query(
        """
        select pi
        from ProductImage pi
        where pi.product.id in :productIds
          and pi.isPrimary = true
        """,
    )
    fun findPrimaryImagesByProductIds(productIds: Collection<Long>): List<ProductImage>

    @Query(
        """
        select pi
        from ProductImage pi
        where pi.product.id = :productId
        order by pi.sortOrder asc
        """,
    )
    fun findAllByProductId(productId: Long): List<ProductImage>
}
