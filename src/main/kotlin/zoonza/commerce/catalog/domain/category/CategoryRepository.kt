package zoonza.commerce.catalog.domain.category

interface CategoryRepository {
    fun findSelfAndDescendantIds(categoryId: Long): Set<Long>
}