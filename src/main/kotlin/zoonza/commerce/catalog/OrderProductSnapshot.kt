package zoonza.commerce.catalog

import zoonza.commerce.shared.Money

data class OrderProductSnapshot(
    val productName: String,
    val optionColor: String,
    val optionSize: String,
    val unitPrice: Money,
)
