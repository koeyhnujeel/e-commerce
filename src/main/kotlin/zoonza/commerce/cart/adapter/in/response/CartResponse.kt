package zoonza.commerce.cart.adapter.`in`.response

import zoonza.commerce.cart.application.dto.CartItemUnavailableReason
import zoonza.commerce.cart.application.dto.CartItemView
import zoonza.commerce.cart.application.dto.CartSummaryView
import zoonza.commerce.cart.application.dto.CartView

data class CartResponse(
    val items: List<CartItemResponse>,
    val summary: CartSummaryResponse,
) {
    companion object {
        fun from(cartView: CartView): CartResponse {
            return CartResponse(
                items = cartView.items.map(CartItemResponse::from),
                summary = CartSummaryResponse.from(cartView.summary),
            )
        }
    }
}

data class CartItemResponse(
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
) {
    companion object {
        fun from(item: CartItemView): CartItemResponse {
            return CartItemResponse(
                productId = item.productId,
                productOptionId = item.productOptionId,
                productName = item.productName,
                primaryImageUrl = item.primaryImageUrl,
                color = item.color,
                size = item.size,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                lineAmount = item.lineAmount,
                purchasable = item.purchasable,
                unavailableReason = item.unavailableReason,
            )
        }
    }
}

data class CartSummaryResponse(
    val itemCount: Int,
    val totalQuantity: Long,
    val totalAmount: Long,
    val purchasableItemCount: Int,
    val purchasableAmount: Long,
) {
    companion object {
        fun from(summary: CartSummaryView): CartSummaryResponse {
            return CartSummaryResponse(
                itemCount = summary.itemCount,
                totalQuantity = summary.totalQuantity,
                totalAmount = summary.totalAmount,
                purchasableItemCount = summary.purchasableItemCount,
                purchasableAmount = summary.purchasableAmount,
            )
        }
    }
}
