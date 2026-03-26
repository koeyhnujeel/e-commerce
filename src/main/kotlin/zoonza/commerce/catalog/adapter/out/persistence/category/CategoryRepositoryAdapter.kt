package zoonza.commerce.catalog.adapter.out.persistence.category

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.domain.category.CategoryRepository

@Repository
class CategoryRepositoryAdapter(
    private val entityManager: EntityManager,
) : CategoryRepository {
    override fun findAllDescendantIds(categoryId: Long): Set<Long> {
        val query = entityManager.createNativeQuery(
            """
            WITH RECURSIVE category_tree AS (
                SELECT id
                FROM category
                WHERE id = :categoryId
                
                UNION ALL
                
                SELECT child.id
                FROM category child
                INNER JOIN category_tree parent_tree ON child.parent_id = parent_tree.id
            )
            SELECT id
            FROM category_tree
            """.trimIndent(),
        )
        query.setParameter("categoryId", categoryId)

        return query.resultList
            .mapTo(linkedSetOf()) { result -> (result as Number).toLong() }
    }
}
