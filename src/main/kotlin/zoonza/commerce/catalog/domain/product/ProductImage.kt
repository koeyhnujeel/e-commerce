package zoonza.commerce.catalog.domain.product

class ProductImage(
    val id: Long = 0,
    val imageUrl: String,
    val isPrimary: Boolean,
    val sortOrder: Int,
)
