package zoonza.commerce.catalog.adapter.`in`.response

import zoonza.commerce.catalog.application.dto.CategoryRoot

data class CategoryRootResponse(
    val id: Long,
    val name: String,
    val sortOrder: Int,
) {
    companion object {
        fun from(category: CategoryRoot): CategoryRootResponse {
            return CategoryRootResponse(
                id = category.id,
                name = category.name,
                sortOrder = category.sortOrder,
            )
        }
    }
}
