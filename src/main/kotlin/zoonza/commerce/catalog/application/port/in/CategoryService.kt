package zoonza.commerce.catalog.application.port.`in`

import zoonza.commerce.catalog.application.dto.CategoryNode
import zoonza.commerce.catalog.application.dto.CategoryRoot
import zoonza.commerce.catalog.application.dto.SubCategory

interface CategoryService {
    fun getRootCategories(): List<CategoryRoot>

    fun getAllCategories(): List<CategoryNode>

    fun getSubCategories(categoryId: Long): List<SubCategory>
}
