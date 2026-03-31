package zoonza.commerce.cart.adapter.`in`.request

import jakarta.validation.constraints.Positive

data class ChangeCartItemQuantityRequest(
    @field:Positive(message = "수량은 1 이상이어야 합니다.")
    val quantity: Long,
)
