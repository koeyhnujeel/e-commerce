package zoonza.commerce.catalog.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.application.dto.CategoryNode
import zoonza.commerce.catalog.application.dto.CategoryRoot
import zoonza.commerce.catalog.application.dto.SubCategory
import zoonza.commerce.catalog.application.port.`in`.CategoryService
import zoonza.commerce.catalog.domain.category.Category
import zoonza.commerce.catalog.domain.category.CategoryErrorCode
import zoonza.commerce.catalog.domain.category.CategoryRepository
import zoonza.commerce.shared.BusinessException

@Service
class DefaultCategoryService(
    private val categoryRepository: CategoryRepository,
) : CategoryService {
    @Transactional(readOnly = true)
    override fun getAllCategories(): List<CategoryNode> {
        val categories = categoryRepository.findAll()

        val subCategoriesByRootCategory = categories
            .filterNot(Category::isRoot)
            .groupBy(Category::rootCategoryId)

        return categories
            .filter(Category::isRoot)
            .map { root -> CategoryNode(
                    id = root.id,
                    name = root.name,
                    sortOrder = root.sortOrder,
                    sub = subCategoriesByRootCategory[root.id]
                        .orEmpty()
                        .map(::toSubNode),
                )
            }
    }

    @Transactional(readOnly = true)
    override fun getRootCategories(): List<CategoryRoot> {
        return categoryRepository.findRootCategories()
            .map { root -> CategoryRoot(
                    id = root.id,
                    name = root.name,
                    sortOrder = root.sortOrder
                )
            }
    }

    @Transactional(readOnly = true)
    override fun getSubCategories(categoryId: Long): List<SubCategory> {
        val selectedCategory = categoryRepository.findById(categoryId)
            ?: throw BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND)

        if (!selectedCategory.isRoot()) {
            throw BusinessException(CategoryErrorCode.ROOT_CATEGORY_REQUIRED)
        }

        return categoryRepository.findSubCategories(selectedCategory.id)
            .map(::toSubCategory)
    }

    private fun toSubNode(category: Category): CategoryNode {
        return CategoryNode(
            id = category.id,
            name = category.name,
            sortOrder = category.sortOrder,
        )
    }

    private fun toSubCategory(category: Category): SubCategory {
        return SubCategory(
            id = category.id,
            name = category.name,
            sortOrder = category.sortOrder,
        )
    }
}
