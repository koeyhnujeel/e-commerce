package zoonza.commerce.catalog.adapter.out.persistence.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import zoonza.commerce.catalog.domain.product.ProductImage

@Entity
@Table(
    name = "product_image",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_product_image_product_id_sort_order",
            columnNames = ["product_id", "sort_order"],
        ),
    ],
)
class ProductImageJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "image_url", nullable = false)
    val imageUrl: String = "",

    @Column(name = "is_primary", nullable = false)
    val isPrimary: Boolean = false,

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int = 0,
) {
    companion object {
        fun from(image: ProductImage): ProductImageJpaEntity {
            return ProductImageJpaEntity(
                id = image.id,
                imageUrl = image.imageUrl,
                isPrimary = image.isPrimary,
                sortOrder = image.sortOrder,
            )
        }
    }

    fun toDomain(): ProductImage {
        return ProductImage(
            id = id,
            imageUrl = imageUrl,
            isPrimary = isPrimary,
            sortOrder = sortOrder,
        )
    }
}
