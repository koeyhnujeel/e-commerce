package zoonza.commerce.catalog.adapter.out.persistence.product

import jakarta.persistence.*
import zoonza.commerce.catalog.domain.product.Product
import zoonza.commerce.catalog.domain.product.ProductSaleStatus
import zoonza.commerce.shared.Money
import java.math.BigDecimal

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

    @Column(name = "base_price", nullable = false, precision = 19, scale = 0)
    val basePrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "category_id", nullable = false)
    val categoryId: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_status", nullable = false, length = 50)
    val saleStatus: ProductSaleStatus = ProductSaleStatus.AVAILABLE,

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
                basePrice = product.basePrice.amount,
                categoryId = product.categoryId,
                saleStatus = product.saleStatus,
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
            basePrice = Money(basePrice),
            categoryId = categoryId,
            saleStatus = saleStatus,
            images = images.map(ProductImageJpaEntity::toDomain).toMutableList(),
            options = options.map(ProductOptionJpaEntity::toDomain).toMutableList(),
        )
    }
}
