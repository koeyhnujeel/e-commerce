package zoonza.commerce.catalog.adapter.`in`.response

import zoonza.commerce.catalog.application.dto.SubCategory

data class SubCategoryResponse(
    val id: Long,
    val name: String,
    val sortOrder: Int,
) {
    companion object {
        fun from(category: SubCategory): SubCategoryResponse {
            return SubCategoryResponse(
                id = category.id,
                name = category.name,
                sortOrder = category.sortOrder,
            )
        }
    }
}
