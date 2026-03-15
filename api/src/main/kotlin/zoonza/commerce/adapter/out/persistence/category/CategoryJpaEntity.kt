package zoonza.commerce.adapter.out.persistence.category

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
import zoonza.commerce.category.Category

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
class CategoryJpaEntity private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: CategoryJpaEntity? = null,

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    val children: MutableList<CategoryJpaEntity> = mutableListOf(),
) {
    companion object {
        fun from(category: Category): CategoryJpaEntity {
            return from(category = category, parent = null)
        }

        private fun from(
            category: Category,
            parent: CategoryJpaEntity?,
        ): CategoryJpaEntity {
            val entity = CategoryJpaEntity(
                id = category.id,
                name = category.name,
                sortOrder = category.sortOrder,
                parent = parent,
                children = mutableListOf(),
            )

            parent?.addChild(entity)
            category.children.forEach { child ->
                from(category = child, parent = entity)
            }

            return entity
        }
    }

    fun toDomain(): Category {
        return toDomain(parent = null)
    }

    private fun toDomain(parent: Category?): Category {
        val category =
            if (parent == null) {
                Category.createRoot(
                    name = name,
                    sortOrder = sortOrder,
                    id = id,
                )
            } else {
                Category.createChild(
                    parent = parent,
                    name = name,
                    sortOrder = sortOrder,
                    id = id,
                )
            }

        children.sortedBy(CategoryJpaEntity::sortOrder)
            .forEach { child -> child.toDomain(parent = category) }

        return category
    }

    private fun addChild(child: CategoryJpaEntity) {
        check(child.parent === this) { "카테고리 부모-자식 연관관계가 올바르지 않습니다." }
        children.add(child)
    }
}
