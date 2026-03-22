package zoonza.commerce.catalog

import zoonza.commerce.shared.Money

data class OrderProductSnapshot(
    val productName: String,
    val option: ProductOptionSnapshot,
    val unitPrice: Money,
)
