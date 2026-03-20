package zoonza.commerce.catalog.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "category",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_category_parent_id_sort_order",
            columnNames = ["parent_id", "sort_order"],
        ),
    ],
)
class Category private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null,

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("sortOrder ASC")
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

            val child = Category(
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
        parent?.sortChildren()
    }

    fun isLeaf(): Boolean {
        return children.isEmpty()
    }

    private fun addChild(child: Category) {
        ensureChildSortOrderAvailable(child.sortOrder)

        check(child.parent === this) { "카테고리 부모-자식 연관관계가 올바르지 않습니다." }

        children.add(child)
        sortChildren()

        check(children.contains(child)) { "카테고리 자식 목록이 올바르지 않습니다." }
    }

    private fun sortChildren() {
        children.sortBy(Category::sortOrder)
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
