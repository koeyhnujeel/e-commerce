package zoonza.commerce.cart.adapter.`in`

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import zoonza.commerce.cart.adapter.`in`.request.AddCartItemRequest
import zoonza.commerce.cart.adapter.`in`.request.ChangeCartItemQuantityRequest
import zoonza.commerce.cart.adapter.`in`.response.CartResponse
import zoonza.commerce.cart.application.port.`in`.CartService
import zoonza.commerce.security.CurrentMember
import zoonza.commerce.support.web.ApiResponse

@RestController
@RequestMapping("/api/cart")
class CartController(
    private val cartService: CartService,
) {
    @GetMapping
    fun getMyCart(
        @AuthenticationPrincipal currentMember: CurrentMember,
    ): ApiResponse<CartResponse> {
        val cart = cartService.getMyCart(currentMember.memberId)
        return ApiResponse.success(CartResponse.from(cart))
    }

    @PostMapping("/items")
    fun addItem(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @Valid @RequestBody request: AddCartItemRequest,
    ): ApiResponse<Nothing> {
        cartService.addItem(
            memberId = currentMember.memberId,
            productId = request.productId,
            productOptionId = request.productOptionId,
            quantity = request.quantity,
        )
        return ApiResponse.success()
    }

    @PatchMapping("/items/{productOptionId}")
    fun changeItemQuantity(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable productOptionId: Long,
        @Valid @RequestBody request: ChangeCartItemQuantityRequest,
    ): ApiResponse<Nothing> {
        cartService.changeItemQuantity(
            memberId = currentMember.memberId,
            productOptionId = productOptionId,
            quantity = request.quantity,
        )
        return ApiResponse.success()
    }

    @DeleteMapping("/items/{productOptionId}")
    fun removeItem(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable productOptionId: Long,
    ): ApiResponse<Nothing> {
        cartService.removeItem(currentMember.memberId, productOptionId)
        return ApiResponse.success()
    }

    @DeleteMapping
    fun clearCart(
        @AuthenticationPrincipal currentMember: CurrentMember,
    ): ApiResponse<Nothing> {
        cartService.clearCart(currentMember.memberId)
        return ApiResponse.success()
    }
}
