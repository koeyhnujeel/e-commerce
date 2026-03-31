package zoonza.commerce.catalog.adapter.out.persistence.category

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.catalog.domain.category.Category
import zoonza.commerce.catalog.domain.category.CategoryRepository

@Repository
class CategoryRepositoryAdapter(
    private val categoryJpaRepository: CategoryJpaRepository,
) : CategoryRepository {
    override fun findRootCategories(): List<Category> {
        return categoryJpaRepository.findAllByRootCategoryIdIsNullOrderBySortOrderAscIdAsc()
            .map(CategoryJpaEntity::toDomain)
    }

    override fun findAll(): List<Category> {
        return categoryJpaRepository.findAllByOrderByDepthAscSortOrderAscIdAsc()
            .map(CategoryJpaEntity::toDomain)
    }

    override fun findById(id: Long): Category? {
        return categoryJpaRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun findSubCategories(rootCategoryId: Long): List<Category> {
        return categoryJpaRepository.findAllByRootCategoryIdOrderBySortOrderAscIdAsc(rootCategoryId)
            .map(CategoryJpaEntity::toDomain)
    }

    override fun findSelfAndSubCategoryIds(categoryId: Long): Set<Long> {
        return categoryJpaRepository.findSelfAndSubCategoryIds(categoryId)
    }
}
