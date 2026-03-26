package zoonza.commerce.catalog.adapter.out.persistence.category

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import zoonza.commerce.catalog.domain.category.Category

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
class CategoryJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(name = "parent_id")
    val parentId: Long? = null,

    @Column(nullable = false)
    val depth: Int = 0,

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int = 0,
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            parentId = parentId,
            depth = depth,
            sortOrder = sortOrder,
        )
    }
}
