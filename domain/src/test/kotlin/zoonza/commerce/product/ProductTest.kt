package zoonza.commerce.product

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import zoonza.commerce.common.Money

class ProductTest {
    @Test
    fun `상품을 생성한다`() {
        val product =
            Product.create(
                brandId = 1L,
                name = "오버핏 셔츠",
                description = "코튼 소재의 기본 셔츠",
                basePrice = Money(39_000L),
                categoryIds = listOf(100L, 200L),
                images = listOf(primaryImage(), secondaryImage()),
                options = listOf(whiteOption(), blackOption()),
            )

        product.brandId shouldBe 1L
        product.name shouldBe "오버핏 셔츠"
        product.description shouldBe "코튼 소재의 기본 셔츠"
        product.basePrice shouldBe Money(39_000L)
        product.categoryIds shouldBe setOf(100L, 200L)
        product.images.map { it.imageUrl } shouldContainExactly
            listOf(
                "https://cdn.example.com/products/1/main.jpg",
                "https://cdn.example.com/products/1/detail.jpg",
            )
        product.options.map { it.color to it.size } shouldContainExactlyInAnyOrder
            listOf(
                "화이트" to "S",
                "블랙" to "M",
            )
    }

    @Test
    fun `상품명은 비어 있을 수 없다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(name = "  ")
            }

        exception.message shouldBe "상품명은 비어 있을 수 없습니다."
    }

    @Test
    fun `상품 설명은 비어 있을 수 없다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(description = " ")
            }

        exception.message shouldBe "상품 설명은 비어 있을 수 없습니다."
    }

    @Test
    fun `Money는 0 이상이어야 한다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                Money(-1L)
            }

        exception.message shouldBe "금액은 0 이상이어야 합니다."
    }

    @Test
    fun `상품 카테고리는 최소 1개 필요하다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(categoryIds = emptyList())
            }

        exception.message shouldBe "상품 카테고리는 최소 1개 필요합니다."
    }

    @Test
    fun `상품 카테고리는 중복될 수 없다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(categoryIds = listOf(100L, 100L))
            }

        exception.message shouldBe "상품 카테고리는 중복될 수 없습니다."
    }

    @Test
    fun `상품 이미지는 최소 1개 필요하다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(images = emptyList())
            }

        exception.message shouldBe "상품 이미지는 최소 1개 필요합니다."
    }

    @Test
    fun `대표 상품 이미지는 정확히 1개여야 한다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(
                    images =
                        listOf(
                            ProductImage.create(
                                imageUrl = "https://cdn.example.com/products/1/main.jpg",
                                isPrimary = true,
                                sortOrder = 0,
                            ),
                            ProductImage.create(
                                imageUrl = "https://cdn.example.com/products/1/detail.jpg",
                                isPrimary = true,
                                sortOrder = 1,
                            ),
                        ),
                )
            }

        exception.message shouldBe "대표 상품 이미지는 정확히 1개여야 합니다."
    }

    @Test
    fun `상품 이미지 정렬 순서는 중복될 수 없다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(
                    images =
                        listOf(
                            ProductImage.create(
                                imageUrl = "https://cdn.example.com/products/1/main.jpg",
                                isPrimary = true,
                                sortOrder = 0,
                            ),
                            ProductImage.create(
                                imageUrl = "https://cdn.example.com/products/1/detail.jpg",
                                isPrimary = false,
                                sortOrder = 0,
                            ),
                        ),
                )
            }

        exception.message shouldBe "상품 이미지 정렬 순서는 중복될 수 없습니다."
    }

    @Test
    fun `상품 옵션은 최소 1개 필요하다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(options = emptyList())
            }

        exception.message shouldBe "상품 옵션은 최소 1개 필요합니다."
    }

    @Test
    fun `상품 옵션 조합은 중복될 수 없다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(
                    options =
                        listOf(
                            ProductOption.create(
                                color = "화이트",
                                size = "M",
                                stockId = 10L,
                            ),
                            ProductOption.create(
                                color = "화이트",
                                size = "M",
                                stockId = 20L,
                            ),
                        ),
                )
            }

        exception.message shouldBe "상품 옵션 조합은 중복될 수 없습니다."
    }

    @Test
    fun `상품 옵션 재고 ID는 중복될 수 없다`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                createProduct(
                    options =
                        listOf(
                            ProductOption.create(
                                color = "화이트",
                                size = "S",
                                stockId = 10L,
                            ),
                            ProductOption.create(
                                color = "블랙",
                                size = "M",
                                stockId = 10L,
                            ),
                        ),
                )
            }

        exception.message shouldBe "상품 옵션 재고 ID는 중복될 수 없습니다."
    }

    @Test
    fun `상품 기본 정보를 변경한다`() {
        val product = createProduct()

        product.changeBasicInfo(
            name = "슬림핏 셔츠",
            description = "새로운 설명",
            basePrice = Money(49_000L),
        )

        product.name shouldBe "슬림핏 셔츠"
        product.description shouldBe "새로운 설명"
        product.basePrice shouldBe Money(49_000L)
    }

    @Test
    fun `상품 카테고리를 교체한다`() {
        val product = createProduct()

        product.replaceCategoryIds(listOf(300L, 400L))

        product.categoryIds shouldBe setOf(300L, 400L)
    }

    @Test
    fun `상품 이미지를 교체한다`() {
        val product = createProduct()
        val replacementImages =
            listOf(
                ProductImage.create(
                    imageUrl = "https://cdn.example.com/products/1/new-main.jpg",
                    isPrimary = true,
                    sortOrder = 0,
                ),
                ProductImage.create(
                    imageUrl = "https://cdn.example.com/products/1/new-detail.jpg",
                    isPrimary = false,
                    sortOrder = 1,
                ),
            )

        product.replaceImages(replacementImages)

        product.images.map { it.imageUrl } shouldContainExactly
            listOf(
                "https://cdn.example.com/products/1/new-main.jpg",
                "https://cdn.example.com/products/1/new-detail.jpg",
            )
    }

    @Test
    fun `상품 옵션을 교체한다`() {
        val product = createProduct()
        val replacementOptions =
            listOf(
                ProductOption.create(
                    color = "네이비",
                    size = "L",
                    stockId = 30L,
                ),
                ProductOption.create(
                    color = "그레이",
                    size = "XL",
                    stockId = 40L,
                ),
            )

        product.replaceOptions(replacementOptions)

        product.options.map { it.color to it.size } shouldContainExactly
            listOf(
                "네이비" to "L",
                "그레이" to "XL",
            )
    }

    private fun createProduct(
        brandId: Long = 1L,
        name: String = "오버핏 셔츠",
        description: String = "코튼 소재의 기본 셔츠",
        basePrice: Money = Money(39_000L),
        categoryIds: Collection<Long> = listOf(100L, 200L),
        images: List<ProductImage> = listOf(primaryImage(), secondaryImage()),
        options: List<ProductOption> = listOf(whiteOption(), blackOption()),
    ): Product {
        return Product.create(
            brandId = brandId,
            name = name,
            description = description,
            basePrice = basePrice,
            categoryIds = categoryIds,
            images = images,
            options = options,
        )
    }

    private fun primaryImage(): ProductImage {
        return ProductImage.create(
            imageUrl = "https://cdn.example.com/products/1/main.jpg",
            isPrimary = true,
            sortOrder = 0,
        )
    }

    private fun secondaryImage(): ProductImage {
        return ProductImage.create(
            imageUrl = "https://cdn.example.com/products/1/detail.jpg",
            isPrimary = false,
            sortOrder = 1,
        )
    }

    private fun whiteOption(): ProductOption {
        return ProductOption.create(
            color = "화이트",
            size = "S",
            stockId = 10L,
        )
    }

    private fun blackOption(): ProductOption {
        return ProductOption.create(
            color = "블랙",
            size = "M",
            stockId = 20L,
        )
    }
}
