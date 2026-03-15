package zoonza.commerce.adapter.out.persistence.product

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import zoonza.commerce.common.Money
import zoonza.commerce.product.Product

@Entity
@Table(name = "product")
class ProductJpaEntity private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "brand_id", nullable = false)
    val brandId: Long,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Column(name = "base_price", nullable = false)
    var basePrice: Long,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "product_category",
        joinColumns = [JoinColumn(name = "product_id")],
        uniqueConstraints = [
            UniqueConstraint(
                name = "uk_product_category_product_id_category_id",
                columnNames = ["product_id", "category_id"],
            ),
        ],
    )
    @Column(name = "category_id", nullable = false)
    val categoryIds: MutableSet<Long> = linkedSetOf(),

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    val images: MutableList<ProductImageJpaEntity> = mutableListOf(),

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val options: MutableList<ProductOptionJpaEntity> = mutableListOf(),
) {
    fun toDomain(): Product {
        return Product.create(
            id = id,
            brandId = brandId,
            name = name,
            description = description,
            basePrice = Money(basePrice),
            categoryIds = categoryIds.toList(),
            images = images.sortedBy(ProductImageJpaEntity::sortOrder).map(ProductImageJpaEntity::toDomain),
            options = options.map(ProductOptionJpaEntity::toDomain),
        )
    }

    companion object {
        fun from(product: Product): ProductJpaEntity {
            val entity = ProductJpaEntity(
                    id = product.id,
                    brandId = product.brandId,
                    name = product.name,
                    description = product.description,
                    basePrice = product.basePrice.amount,
                )

            entity.categoryIds.addAll(product.categoryIds)
            entity.images.addAll(product.images.map { ProductImageJpaEntity.from(image = it, product = entity) })
            entity.options.addAll(product.options.map { ProductOptionJpaEntity.from(option = it, product = entity) })

            return entity
        }
    }
}