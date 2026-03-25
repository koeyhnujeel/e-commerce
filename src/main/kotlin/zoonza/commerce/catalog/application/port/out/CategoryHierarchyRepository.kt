package zoonza.commerce.catalog.application.port.out

interface CategoryHierarchyRepository {
    fun findSelfAndDescendantIds(categoryId: Long): Set<Long>
}
