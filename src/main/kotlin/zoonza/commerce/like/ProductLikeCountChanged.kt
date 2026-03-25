package zoonza.commerce.like

data class ProductLikeCountChanged(
    val productId: Long,
    val delta: Long,
)
