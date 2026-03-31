package zoonza.commerce.catalog.adapter.`in`.response

import zoonza.commerce.catalog.application.dto.CategoryNode

data class CategoryTreeResponse(
    val id: Long,
    val name: String,
    val sortOrder: Int,
    val sub: List<CategoryTreeResponse>,
) {
    companion object {
        fun from(category: CategoryNode): CategoryTreeResponse {
            return CategoryTreeResponse(
                id = category.id,
                name = category.name,
                sortOrder = category.sortOrder,
                sub = category.sub.map(::from),
            )
        }
    }
}
