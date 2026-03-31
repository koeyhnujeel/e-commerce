package zoonza.commerce.catalog.domain.category

interface CategoryRepository {
    fun findRootCategories(): List<Category>

    fun findAll(): List<Category>

    fun findById(id: Long): Category?

    fun findSubCategories(rootCategoryId: Long): List<Category>

    fun findSelfAndSubCategoryIds(categoryId: Long): Set<Long>
}
