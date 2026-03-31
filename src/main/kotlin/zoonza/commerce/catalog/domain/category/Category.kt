package zoonza.commerce.catalog.domain.category

class Category(
    val id: Long = 0,
    val name: String,
    val rootCategoryId: Long? = null,
    val depth: Int = 0,
    val sortOrder: Int = 0,
) {
    init {
        require(depth == 0 || depth == 1) { "카테고리 depth는 0 또는 1이어야 합니다." }
        require((depth == 0 && rootCategoryId == null) || (depth == 1 && rootCategoryId != null)) {
            "카테고리 depth와 rootCategoryId 조합이 올바르지 않습니다."
        }
    }

    fun isRoot(): Boolean {
        return rootCategoryId == null
    }
}
