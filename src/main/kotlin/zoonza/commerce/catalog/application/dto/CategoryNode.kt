package zoonza.commerce.catalog.application.dto

data class CategoryNode(
    val id: Long,
    val name: String,
    val sortOrder: Int,
    val sub: List<CategoryNode> = emptyList(),
)
