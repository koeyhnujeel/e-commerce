package zoonza.commerce.catalog.domain.category

interface CategoryRepository {
    fun findAllDescendantIds(categoryId: Long): Set<Long>
}