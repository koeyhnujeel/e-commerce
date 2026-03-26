package zoonza.commerce.catalog.domain.product

import zoonza.commerce.shared.Money

class ProductOption(
    val id: Long = 0,
    val color: String,
    val size: String,
    val sortOder: Int,
    val additionalPrice: Money,
)
