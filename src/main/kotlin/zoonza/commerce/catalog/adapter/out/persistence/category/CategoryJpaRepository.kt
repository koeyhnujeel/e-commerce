package zoonza.commerce.catalog.adapter.out.persistence.category

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CategoryJpaRepository : JpaRepository<CategoryJpaEntity, Long> {
    fun findAllByRootCategoryIdIsNullOrderBySortOrderAscIdAsc(): List<CategoryJpaEntity>

    fun findAllByOrderByDepthAscSortOrderAscIdAsc(): List<CategoryJpaEntity>

    fun findAllByRootCategoryIdOrderBySortOrderAscIdAsc(rootCategoryId: Long): List<CategoryJpaEntity>

    @Query(
        """
        select c.id
        from CategoryJpaEntity c
        where c.id = :categoryId or c.rootCategoryId = :categoryId
        """,
    )
    fun findSelfAndSubCategoryIds(categoryId: Long): Set<Long>
}
