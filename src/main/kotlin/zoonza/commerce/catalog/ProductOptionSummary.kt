package zoonza.commerce.catalog

data class ProductOptionSummary(
    val productId: Long,
    val productOptionId: Long,
    val productName: String,
    val primaryImageUrl: String?,
    val basePrice: Long,
    val additionalPrice: Long,
    val color: String,
    val size: String,
    val availableForSale: Boolean,
)
