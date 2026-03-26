package zoonza.commerce.catalog.adapter.out.persistence.product

import jakarta.persistence.*
import zoonza.commerce.catalog.domain.product.Product
import zoonza.commerce.shared.Money

@Entity
@Table(name = "product")
class ProductJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "brand_id", nullable = false)
    val brandId: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = false, columnDefinition = "TEXT")
    val description: String = "",

    @Embedded
    @AttributeOverride(
        name = "amount",
        column = Column(name = "base_price", nullable = false),
    )
    val basePrice: Money = Money(0),

    @Column(name = "category_id", nullable = false)
    val categoryId: Long = 0,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "product_id", nullable = false)
    @OrderBy("sortOrder ASC")
    val images: MutableList<ProductImageJpaEntity> = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "product_id", nullable = false)
    @OrderBy("sortOrder ASC")
    val options: MutableList<ProductOptionJpaEntity> = mutableListOf(),
) {
    companion object {
        fun from(product: Product): ProductJpaEntity {
            return ProductJpaEntity(
                id = product.id,
                brandId = product.brandId,
                name = product.name,
                description = product.description,
                basePrice = product.basePrice,
                categoryId = product.categoryId,
                images = product.images.map(ProductImageJpaEntity::from).toMutableList(),
                options = product.options.map(ProductOptionJpaEntity::from).toMutableList(),
            )
        }
    }

    fun toDomain(): Product {
        return Product(
            id = id,
            brandId = brandId,
            name = name,
            description = description,
            basePrice = basePrice,
            categoryId = categoryId,
            images = images.map(ProductImageJpaEntity::toDomain).toMutableList(),
            options = options.map(ProductOptionJpaEntity::toDomain).toMutableList(),
        )
    }
}
