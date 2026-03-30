package zoonza.commerce.catalog.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import zoonza.commerce.catalog.domain.product.Product
import zoonza.commerce.catalog.domain.product.ProductImage
import zoonza.commerce.catalog.domain.product.ProductOption
import zoonza.commerce.shared.Money
import java.math.BigDecimal

class ProductTest {
    @Test
    fun `상품은 현재 도메인 필드를 그대로 보관한다`() {
        val product = product()

        product.id shouldBe 1L
        product.name shouldBe "반팔 티셔츠"
        product.categoryId shouldBe 10L
        product.images.map { it.imageUrl } shouldBe
            listOf(
                "https://cdn.example.com/primary.jpg",
                "https://cdn.example.com/secondary.jpg",
            )
        product.options.map { it.additionalPrice.amount } shouldBe listOf(BigDecimal.ZERO, BigDecimal.valueOf(1_000))
    }

    @Test
    fun `상품 옵션은 추가 금액과 정렬 순서를 가진다`() {
        val option = option(color = "WHITE", size = "L", sortOrder = 1, additionalPrice = 1_000L)

        option.sortOder shouldBe 1
        option.additionalPrice.amount shouldBe BigDecimal.valueOf(1_000)
    }

    private fun primaryImage(): ProductImage {
        return ProductImage(
            imageUrl = "https://cdn.example.com/primary.jpg",
            isPrimary = true,
            sortOrder = 0,
        )
    }

    private fun secondaryImage(): ProductImage {
        return ProductImage(
            imageUrl = "https://cdn.example.com/secondary.jpg",
            isPrimary = false,
            sortOrder = 1,
        )
    }

    private fun option(
        color: String,
        size: String,
        sortOrder: Int,
        additionalPrice: Long = 0L,
    ): ProductOption {
        return ProductOption(
            color = color,
            size = size,
            sortOder = sortOrder,
            additionalPrice = Money(additionalPrice),
        )
    }

    private fun product(): Product {
        return Product(
            id = 1L,
            brandId = 1L,
            name = "반팔 티셔츠",
            description = "여름 기본 아이템",
            basePrice = Money(19_900),
            categoryId = 10L,
            images = mutableListOf(primaryImage(), secondaryImage()),
            options =
                mutableListOf(
                    option(color = "BLACK", size = "M", sortOrder = 0),
                    option(color = "WHITE", size = "L", sortOrder = 1, additionalPrice = 1_000L),
                ),
        )
    }
}
