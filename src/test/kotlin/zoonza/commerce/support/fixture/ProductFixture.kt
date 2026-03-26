package zoonza.commerce.support.fixture

import zoonza.commerce.catalog.adapter.out.persistence.product.ProductEntity
import zoonza.commerce.catalog.adapter.out.persistence.product.ProductMapper
import zoonza.commerce.catalog.domain.product.Product
import zoonza.commerce.catalog.domain.product.ProductImage
import zoonza.commerce.catalog.domain.product.ProductOption
import zoonza.commerce.shared.Money

object ProductFixture {
    fun createSingleOption(
        index: Int,
        namePrefix: String = "상품",
        descriptionPrefix: String = "상품 설명",
        price: Long = 19_900,
        categoryId: Long = 1L,
        imagePrefix: String = "product",
        color: String = "BLACK",
        size: String = "M",
        additionalPrice: Long = 0,
    ): ProductEntity {
        return ProductMapper.toEntity(
            Product(
                brandId = 1L,
                name = "$namePrefix$index",
                description = "$descriptionPrefix$index",
                basePrice = Money(price),
                categoryId = categoryId,
                images =
                    mutableListOf(
                        ProductImage(
                            imageUrl = "https://cdn.example.com/$imagePrefix-$index-primary.jpg",
                            isPrimary = true,
                            sortOrder = 0,
                        ),
                    ),
                options =
                    mutableListOf(
                        ProductOption(
                            color = color,
                            size = size,
                            sortOder = 0,
                            additionalPrice = Money(additionalPrice),
                        ),
                    ),
            ),
        )
    }

    fun createCatalogProduct(
        index: Int,
        price: Long,
        categoryId: Long,
        namePrefix: String = "상품",
        descriptionPrefix: String = "상품 설명",
        imagePrefix: String = "product",
    ): ProductEntity {
        return ProductMapper.toEntity(
            Product(
                brandId = 1L,
                name = "$namePrefix$index",
                description = "$descriptionPrefix$index",
                basePrice = Money(price),
                categoryId = categoryId,
                images =
                    mutableListOf(
                        ProductImage(
                            imageUrl = "https://cdn.example.com/$imagePrefix-$index-primary.jpg",
                            isPrimary = true,
                            sortOrder = 0,
                        ),
                        ProductImage(
                            imageUrl = "https://cdn.example.com/$imagePrefix-$index-secondary.jpg",
                            isPrimary = false,
                            sortOrder = 1,
                        ),
                    ),
                options =
                    mutableListOf(
                        ProductOption(
                            color = "BLACK",
                            size = "M",
                            sortOder = 0,
                            additionalPrice = Money(0),
                        ),
                        ProductOption(
                            color = "WHITE",
                            size = "L",
                            sortOder = 1,
                            additionalPrice = Money(1_000),
                        ),
                    ),
            ),
        )
    }
}
