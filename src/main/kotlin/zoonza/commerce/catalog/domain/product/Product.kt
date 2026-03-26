package zoonza.commerce.catalog.domain.product

import zoonza.commerce.shared.Money

class Product(
    val id: Long = 0,
    val brandId: Long,
    val name: String,
    val description: String,
    val basePrice: Money,
    val categoryId: Long,
    val images: MutableList<ProductImage> = mutableListOf(),
    val options: MutableList<ProductOption> = mutableListOf(),
)
