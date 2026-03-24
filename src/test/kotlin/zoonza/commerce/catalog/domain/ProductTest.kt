package zoonza.commerce.catalog.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import zoonza.commerce.shared.Money

class ProductTest {
    @Test
    fun `상품을 생성하면 기본 정보를 정규화한다`() {
        val product =
            Product.create(
                brandId = 1L,
                name = "  반팔 티셔츠  ",
                description = "  여름 기본 아이템  ",
                basePrice = Money(19_900),
                categoryIds = listOf(10L, 20L),
                images = listOf(primaryImage(), secondaryImage()),
                options = listOf(option(color = "BLACK", size = "M", stockId = 1L)),
            )

        product.name shouldBe "반팔 티셔츠"
        product.description shouldBe "여름 기본 아이템"
        product.categoryIds shouldBe mutableSetOf(10L, 20L)
    }

    @Test
    fun `상품을 생성하면 이미지와 옵션이 상품에 연결된다`() {
        val product =
            Product.create(
                brandId = 1L,
                name = "반팔 티셔츠",
                description = "여름 기본 아이템",
                basePrice = Money(19_900),
                categoryIds = listOf(10L, 20L),
                images = listOf(secondaryImage(), primaryImage()),
                options =
                    listOf(
                        option(color = "BLACK", size = "M", stockId = 1L),
                        option(color = "BLACK", size = "L", stockId = 2L),
                    ),
            )

        product.images.map { it.sortOrder } shouldBe listOf(0, 1)
        product.options.map { it.color to it.size } shouldBe listOf("BLACK" to "M", "BLACK" to "L")
    }

    @Test
    fun `상품 카테고리는 최소 하나 이상이어야 한다`() {
        shouldThrow<IllegalArgumentException> {
            Product.create(
                brandId = 1L,
                name = "반팔 티셔츠",
                description = "여름 기본 아이템",
                basePrice = Money(19_900),
                categoryIds = emptyList(),
                images = listOf(primaryImage()),
                options = listOf(option(color = "BLACK", size = "M", stockId = 1L)),
            )
        }
    }

    @Test
    fun `대표 상품 이미지는 정확히 하나여야 한다`() {
        shouldThrow<IllegalArgumentException> {
            Product.create(
                brandId = 1L,
                name = "반팔 티셔츠",
                description = "여름 기본 아이템",
                basePrice = Money(19_900),
                categoryIds = listOf(10L),
                images = listOf(primaryImage(), ProductImage.create("https://cdn.example.com/primary-2.jpg", true, 1)),
                options = listOf(option(color = "BLACK", size = "M", stockId = 1L)),
            )
        }
    }

    @Test
    fun `중복된 옵션 조합은 허용하지 않는다`() {
        val product =
            Product.create(
                brandId = 1L,
                name = "반팔 티셔츠",
                description = "여름 기본 아이템",
                basePrice = Money(19_900),
                categoryIds = listOf(10L),
                images = listOf(primaryImage()),
                options = listOf(option(color = "BLACK", size = "M", stockId = 1L)),
            )

        shouldThrow<IllegalArgumentException> {
            product.replaceOptions(
                listOf(
                    option(color = "BLACK", size = "M", stockId = 1L),
                    option(color = "BLACK", size = "M", stockId = 2L),
                ),
            )
        }
    }

    @Test
    fun `상품은 대표 이미지를 조회할 수 있다`() {
        val product =
            Product.create(
                brandId = 1L,
                name = "반팔 티셔츠",
                description = "여름 기본 아이템",
                basePrice = Money(19_900),
                categoryIds = listOf(10L),
                images = listOf(secondaryImage(), primaryImage()),
                options = listOf(option(color = "BLACK", size = "M", stockId = 1L)),
            )

        product.primaryImage().imageUrl shouldBe "https://cdn.example.com/primary.jpg"
    }

    @Test
    fun `주문 가능한 옵션이 있으면 판매 가능 상품이다`() {
        val product =
            Product.create(
                brandId = 1L,
                name = "반팔 티셔츠",
                description = "여름 기본 아이템",
                basePrice = Money(19_900),
                categoryIds = listOf(10L),
                images = listOf(primaryImage()),
                options = listOf(option(color = "BLACK", size = "M", stockId = 1L)),
            )

        product.isAvailableForSale() shouldBe true
        product.saleStatus() shouldBe ProductSaleStatus.AVAILABLE
    }

    private fun primaryImage(): ProductImage {
        return ProductImage.create(
            imageUrl = "https://cdn.example.com/primary.jpg",
            isPrimary = true,
            sortOrder = 0,
        )
    }

    private fun secondaryImage(): ProductImage {
        return ProductImage.create(
            imageUrl = "https://cdn.example.com/secondary.jpg",
            isPrimary = false,
            sortOrder = 1,
        )
    }

    private fun option(
        color: String,
        size: String,
        stockId: Long,
    ): ProductOption {
        return ProductOption.create(
            color = color,
            size = size,
            stockId = stockId,
        )
    }
}
