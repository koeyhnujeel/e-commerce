package zoonza.commerce.catalog.domain

import jakarta.persistence.AttributeOverride
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embedded
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
import zoonza.commerce.shared.Money

@Entity
@Table(name = "product")
class Product private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "brand_id", nullable = false)
    val brandId: Long,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Embedded
    @AttributeOverride(
        name = "amount",
        column = Column(name = "base_price", nullable = false),
    )
    var basePrice: Money,

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
    val categoryIds: MutableSet<Long>,

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    val images: MutableList<ProductImage>,

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val options: MutableList<ProductOption>,
) {
    companion object {
        fun create(
            brandId: Long,
            name: String,
            description: String,
            basePrice: Money,
            categoryIds: Collection<Long>,
            images: List<ProductImage>,
            options: List<ProductOption>,
            id: Long = 0,
        ): Product {
            require(id >= 0) { "상품 ID는 0 이상이어야 합니다." }
            require(brandId > 0) { "브랜드 ID는 1 이상이어야 합니다." }

            val normalizedName = normalizeName(name)
            val normalizedDescription = normalizeDescription(description)
            val validatedCategoryIds = validateCategoryIds(categoryIds)

            validateImages(images)
            validateOptions(options)

            val product = Product(
                    id = id,
                    brandId = brandId,
                    name = normalizedName,
                    description = normalizedDescription,
                    basePrice = basePrice,
                    categoryIds = validatedCategoryIds.toMutableSet(),
                    images = mutableListOf(),
                    options = mutableListOf(),
                )

            product.replaceImages(images)
            product.replaceOptions(options)

            return product
        }

        private fun normalizeName(name: String): String {
            require(name.isNotBlank()) { "상품명은 비어 있을 수 없습니다." }
            return name.trim()
        }

        private fun normalizeDescription(description: String): String {
            require(description.isNotBlank()) { "상품 설명은 비어 있을 수 없습니다." }
            return description.trim()
        }

        private fun validateCategoryIds(categoryIds: Collection<Long>): Set<Long> {
            require(categoryIds.isNotEmpty()) {
                "상품 카테고리는 최소 1개 필요합니다."
            }

            val validatedCategoryIds = linkedSetOf<Long>()

            categoryIds.forEach { categoryId ->
                require(validatedCategoryIds.add(categoryId)) {
                    "상품 카테고리는 중복될 수 없습니다."
                }
            }

            return validatedCategoryIds
        }

        private fun validateImages(images: List<ProductImage>) {
            require(images.isNotEmpty()) { "상품 이미지는 최소 1개 필요합니다." }
            require(images.count { it.isPrimary } == 1) { "대표 상품 이미지는 정확히 1개여야 합니다." }

            val sortOrders = mutableSetOf<Int>()

            images.forEach { image ->
                require(sortOrders.add(image.sortOrder)) {
                    "상품 이미지 정렬 순서는 중복될 수 없습니다."
                }
            }
        }

        private fun validateOptions(options: List<ProductOption>) {
            require(options.isNotEmpty()) { "상품 옵션은 최소 1개 필요합니다." }

            val optionCombinations = mutableSetOf<Pair<String, String>>()
            val stockIds = mutableSetOf<Long>()

            options.forEach { option ->
                require(optionCombinations.add(option.color to option.size)) {
                    "상품 옵션 조합은 중복될 수 없습니다."
                }

                require(stockIds.add(option.stockId)) {
                    "상품 옵션 재고 ID는 중복될 수 없습니다."
                }
            }
        }
    }

    fun changeBasicInfo(
        name: String,
        description: String,
        basePrice: Money,
    ) {
        this.name = normalizeName(name)
        this.description = normalizeDescription(description)
        this.basePrice = basePrice
    }

    fun replaceCategoryIds(categoryIds: Collection<Long>) {
        val validatedCategoryIds = validateCategoryIds(categoryIds)

        this.categoryIds.clear()
        this.categoryIds.addAll(validatedCategoryIds)

        check(this.categoryIds.size == validatedCategoryIds.size) {
            "상품 카테고리 연관관계가 올바르지 않습니다."
        }
    }

    fun replaceImages(images: List<ProductImage>) {
        validateImages(images)

        this.images.clear()

        images.sortedBy(ProductImage::sortOrder)
            .onEach { it.belongTo(this) }
            .forEach(this.images::add)

        check(this.images.size == images.size) { "상품 이미지 목록이 올바르지 않습니다." }
    }

    fun replaceOptions(options: List<ProductOption>) {
        validateOptions(options)

        this.options.clear()
        options.onEach { it.belongTo(this) }
            .forEach(this.options::add)

        check(this.options.size == options.size) { "상품 옵션 목록이 올바르지 않습니다." }
    }

    fun primaryImage(): ProductImage {
        return images.firstOrNull(ProductImage::isPrimary)
            ?: throw IllegalStateException("대표 상품 이미지가 없습니다.")
    }

    fun isAvailableForSale(): Boolean {
        return options.any(ProductOption::isOrderable)
    }

    fun saleStatus(): ProductSaleStatus {
        return if (isAvailableForSale()) {
            ProductSaleStatus.AVAILABLE
        } else {
            ProductSaleStatus.UNAVAILABLE
        }
    }
}
