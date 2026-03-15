package zoonza.commerce.adapter.out.persistence.product

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import zoonza.commerce.common.Money
import zoonza.commerce.product.Product
import zoonza.commerce.product.ProductImage
import zoonza.commerce.product.ProductOption

class ProductJpaEntityTest {
    @Test
    fun `도메인 상품을 JPA 엔티티로 변환한다`() {
        val product = createProduct()

        val entity = ProductJpaEntity.from(product)

        entity.id shouldBe 1L
        entity.brandId shouldBe 10L
        entity.name shouldBe "오버핏 셔츠"
        entity.description shouldBe "코튼 소재의 기본 셔츠"
        entity.basePrice shouldBe 39_000L
        entity.categoryIds shouldContainExactlyInAnyOrder setOf(100L, 200L)
        entity.images.map { it.imageUrl } shouldContainExactly
            listOf(
                "https://cdn.example.com/products/1/main.jpg",
                "https://cdn.example.com/products/1/detail.jpg",
            )
        entity.images.all { it.product === entity } shouldBe true
        entity.options.map { it.color to it.size } shouldContainExactly
            listOf(
                "화이트" to "S",
                "블랙" to "M",
            )
        entity.options.all { it.product === entity } shouldBe true
    }

    @Test
    fun `JPA 엔티티 상품을 도메인 모델로 변환한다`() {
        val entity = ProductJpaEntity.from(createProduct())

        val product = entity.toDomain()

        product.id shouldBe 1L
        product.brandId shouldBe 10L
        product.basePrice shouldBe Money(39_000L)
        product.categoryIds shouldBe setOf(100L, 200L)
        product.images.map { it.id } shouldContainExactly listOf(11L, 12L)
        product.options.map { it.id } shouldContainExactly listOf(21L, 22L)
    }

    private fun createProduct(): Product {
        return Product.create(
            id = 1L,
            brandId = 10L,
            name = "오버핏 셔츠",
            description = "코튼 소재의 기본 셔츠",
            basePrice = Money(39_000L),
            categoryIds = listOf(100L, 200L),
            images =
                listOf(
                    ProductImage.create(
                        id = 11L,
                        imageUrl = "https://cdn.example.com/products/1/main.jpg",
                        isPrimary = true,
                        sortOrder = 0,
                    ),
                    ProductImage.create(
                        id = 12L,
                        imageUrl = "https://cdn.example.com/products/1/detail.jpg",
                        isPrimary = false,
                        sortOrder = 1,
                    ),
                ),
            options =
                listOf(
                    ProductOption.create(
                        id = 21L,
                        color = "화이트",
                        size = "S",
                        stockId = 1001L,
                    ),
                    ProductOption.create(
                        id = 22L,
                        color = "블랙",
                        size = "M",
                        stockId = 1002L,
                    ),
                ),
        )
    }
}
