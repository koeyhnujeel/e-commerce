package zoonza.commerce.catalog.adapter.out.persistence.category

import jakarta.persistence.*
import zoonza.commerce.catalog.domain.category.Category

@Entity
@Table(
    name = "category",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_category_rootCategory_id_sort_order",
            columnNames = ["rootCategory_id", "sort_order"],
        ),
    ],
)
class CategoryJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(name = "rootCategory_id")
    val rootCategoryId: Long? = null,

    @Column(nullable = false)
    val depth: Int = 0,

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int = 0,
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            rootCategoryId = rootCategoryId,
            depth = depth,
            sortOrder = sortOrder,
        )
    }
}
