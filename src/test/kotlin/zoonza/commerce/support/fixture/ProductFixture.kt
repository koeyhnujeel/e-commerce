package zoonza.commerce.support.fixture

import zoonza.commerce.catalog.domain.Product
import zoonza.commerce.catalog.domain.ProductImage
import zoonza.commerce.catalog.domain.ProductOption
import zoonza.commerce.shared.Money

object ProductFixture {
    fun createSingleOption(
        index: Int,
        namePrefix: String = "상품",
        descriptionPrefix: String = "상품 설명",
        price: Long = 19_900,
        categoryIds: List<Long> = listOf(1L),
        imagePrefix: String = "product",
        color: String = "BLACK",
        size: String = "M",
        stockId: Long = index.toLong(),
    ): Product {
        return Product.create(
            brandId = 1L,
            name = "$namePrefix$index",
            description = "$descriptionPrefix$index",
            basePrice = Money(price),
            categoryIds = categoryIds,
            images =
                listOf(
                    ProductImage.create(
                        imageUrl = "https://cdn.example.com/$imagePrefix-$index-primary.jpg",
                        isPrimary = true,
                        sortOrder = 0,
                    ),
                ),
            options =
                listOf(
                    ProductOption.create(
                        color = color,
                        size = size,
                        stockId = stockId,
                    ),
                ),
        )
    }

    fun createCatalogProduct(
        index: Int,
        price: Long,
        categoryIds: List<Long>,
        namePrefix: String = "상품",
        descriptionPrefix: String = "상품 설명",
        imagePrefix: String = "product",
    ): Product {
        return Product.create(
            brandId = 1L,
            name = "$namePrefix$index",
            description = "$descriptionPrefix$index",
            basePrice = Money(price),
            categoryIds = categoryIds,
            images =
                listOf(
                    ProductImage.create(
                        imageUrl = "https://cdn.example.com/$imagePrefix-$index-primary.jpg",
                        isPrimary = true,
                        sortOrder = 0,
                    ),
                    ProductImage.create(
                        imageUrl = "https://cdn.example.com/$imagePrefix-$index-secondary.jpg",
                        isPrimary = false,
                        sortOrder = 1,
                    ),
                ),
            options =
                listOf(
                    ProductOption.create(
                        color = "BLACK",
                        size = "M",
                        stockId = index.toLong() * 10,
                    ),
                    ProductOption.create(
                        color = "WHITE",
                        size = "L",
                        stockId = index.toLong() * 10 + 1,
                    ),
                ),
        )
    }
}
