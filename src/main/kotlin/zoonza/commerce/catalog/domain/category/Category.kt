package zoonza.commerce.catalog.domain.category

class Category(
    val id: Long = 0,
    val name: String,
    val parentId: Long? = null,
    val depth: Int = 0,
    val sortOrder: Int = 0,
) {
    fun isRoot(): Boolean {
        return parentId == null
    }
}
