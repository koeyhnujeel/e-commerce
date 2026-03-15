package zoonza.commerce.adapter.out.persistence.product

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import zoonza.commerce.product.ProductImage

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
class ProductImageJpaEntity private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "image_url", nullable = false)
    val imageUrl: String,

    @Column(name = "is_primary", nullable = false)
    val isPrimary: Boolean,

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: ProductJpaEntity,
) {

    companion object {
        fun from(
            image: ProductImage,
            product: ProductJpaEntity,
        ): ProductImageJpaEntity {
            return ProductImageJpaEntity(
                id = image.id,
                imageUrl = image.imageUrl,
                isPrimary = image.isPrimary,
                sortOrder = image.sortOrder,
                product = product,
            )
        }
    }

    fun toDomain(): ProductImage {
        return ProductImage.create(
            id = id,
            imageUrl = imageUrl,
            isPrimary = isPrimary,
            sortOrder = sortOrder,
        )
    }
}
