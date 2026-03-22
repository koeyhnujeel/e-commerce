package zoonza.commerce.order

import zoonza.commerce.catalog.ProductOptionSnapshot

data class ReviewablePurchase(
    val orderItemId: Long,
    val option: ProductOptionSnapshot,
)
