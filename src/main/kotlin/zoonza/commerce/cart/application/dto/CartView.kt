package zoonza.commerce.cart.application.dto

data class CartView(
    val items: List<CartItemView>,
    val summary: CartSummaryView,
) {
    companion object {
        fun empty(): CartView {
            return CartView(
                items = emptyList(),
                summary =
                    CartSummaryView(
                        itemCount = 0,
                        totalQuantity = 0,
                        totalAmount = 0,
                        purchasableItemCount = 0,
                        purchasableAmount = 0,
                    ),
            )
        }
    }
}

data class CartItemView(
    val productId: Long,
    val productOptionId: Long,
    val productName: String,
    val primaryImageUrl: String?,
    val color: String,
    val size: String,
    val quantity: Long,
    val unitPrice: Long,
    val lineAmount: Long,
    val purchasable: Boolean,
    val unavailableReason: CartItemUnavailableReason?,
)

data class CartSummaryView(
    val itemCount: Int,
    val totalQuantity: Long,
    val totalAmount: Long,
    val purchasableItemCount: Int,
    val purchasableAmount: Long,
)

enum class CartItemUnavailableReason {
    SALE_UNAVAILABLE,
    OUT_OF_STOCK,
    INSUFFICIENT_STOCK,
}
