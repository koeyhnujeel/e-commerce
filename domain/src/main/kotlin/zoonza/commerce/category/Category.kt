package zoonza.commerce.category

class Category private constructor(
    val id: Long = 0,
    var name: String,
    var sortOrder: Int,
    val parent: Category? = null,
    val children: MutableList<Category> = mutableListOf(),
) {
    companion object {
        fun createRoot(
            name: String,
            sortOrder: Int,
            id: Long = 0,
        ): Category {
            validateId(id)

            return Category(
                id = id,
                name = normalizeName(name),
                sortOrder = validateSortOrder(sortOrder),
                parent = null,
                children = mutableListOf(),
            )
        }

        fun createChild(
            parent: Category,
            name: String,
            sortOrder: Int,
            id: Long = 0,
        ): Category {
            validateId(id)

            val child =
                Category(
                    id = id,
                    name = normalizeName(name),
                    sortOrder = validateSortOrder(sortOrder),
                    parent = parent,
                    children = mutableListOf(),
                )

            parent.addChild(child)
            return child
        }

        private fun validateId(id: Long) {
            require(id >= 0) { "카테고리 ID는 0 이상이어야 합니다." }
        }

        private fun normalizeName(name: String): String {
            require(name.isNotBlank()) { "카테고리명은 비어 있을 수 없습니다." }
            return name.trim()
        }

        private fun validateSortOrder(sortOrder: Int): Int {
            require(sortOrder >= 0) { "카테고리 정렬 순서는 0 이상이어야 합니다." }
            return sortOrder
        }
    }

    fun rename(name: String) {
        this.name = normalizeName(name)
    }

    fun changeSortOrder(sortOrder: Int) {
        validateSortOrder(sortOrder)

        parent?.ensureChildSortOrderAvailable(sortOrder, this)

        this.sortOrder = sortOrder
    }

    fun isLeaf(): Boolean {
        return children.isEmpty()
    }

    private fun addChild(child: Category) {
        ensureChildSortOrderAvailable(child.sortOrder)

        check(child.parent === this) { "카테고리 부모-자식 연관관계가 올바르지 않습니다." }

        children.add(child)

        check(children.contains(child)) { "카테고리 자식 목록이 올바르지 않습니다." }
    }

    private fun ensureChildSortOrderAvailable(
        sortOrder: Int,
        current: Category? = null,
    ) {
        require(children.none { it !== current && it.sortOrder == sortOrder }) {
            "형제 카테고리의 정렬 순서는 중복될 수 없습니다."
        }
    }
}
