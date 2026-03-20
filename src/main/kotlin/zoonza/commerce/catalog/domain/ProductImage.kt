package zoonza.commerce.catalog.domain

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
class ProductImage private constructor(
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
    var product: Product? = null,
) {
    companion object {
        fun create(
            imageUrl: String,
            isPrimary: Boolean,
            sortOrder: Int,
            id: Long = 0,
        ): ProductImage {
            return ProductImage(
                id = validateId(id),
                imageUrl = normalizeImageUrl(imageUrl),
                isPrimary = isPrimary,
                sortOrder = validateSortOrder(sortOrder),
            )
        }

        private fun validateId(id: Long): Long {
            require(id >= 0) { "상품 이미지 ID는 0 이상이어야 합니다." }
            return id
        }

        private fun normalizeImageUrl(imageUrl: String): String {
            require(imageUrl.isNotBlank()) { "상품 이미지 URL은 비어 있을 수 없습니다." }
            return imageUrl.trim()
        }

        private fun validateSortOrder(sortOrder: Int): Int {
            require(sortOrder >= 0) { "상품 이미지 정렬 순서는 0 이상이어야 합니다." }
            return sortOrder
        }
    }

    internal fun belongTo(product: Product) {
        this.product = product
    }
}
