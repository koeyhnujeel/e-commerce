package zoonza.commerce.catalog.adapter.out.persistence.product

import jakarta.persistence.*
import zoonza.commerce.shared.Money

@Entity
@Table(name = "product")
class ProductEntity(
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
    val images: MutableList<ProductImageEntity> = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "product_id", nullable = false)
    @OrderBy("sortOrder ASC")
    val options: MutableList<ProductOptionEntity> = mutableListOf(),
)

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
class ProductImageEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "image_url", nullable = false)
    val imageUrl: String = "",

    @Column(name = "is_primary", nullable = false)
    val isPrimary: Boolean = false,

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int = 0,
)

@Entity
@Table(
    name = "product_option",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_product_option_product_id_option_color_option_size",
            columnNames = ["product_id", "option_color", "option_size"],
        ),
        UniqueConstraint(
            name = "uk_product_option_product_id_sort_order",
            columnNames = ["product_id", "sort_order"],
        ),
    ],
)
class ProductOptionEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "option_color", nullable = false)
    val color: String = "",

    @Column(name = "option_size", nullable = false)
    val size: String = "",

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int = 0,

    @Embedded
    @AttributeOverride(
        name = "amount",
        column = Column(name = "additional_price", nullable = false),
    )
    val additionalPrice: Money = Money(0),
)
