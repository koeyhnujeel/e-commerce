package zoonza.commerce.cart.adapter.`in`.request

import jakarta.validation.constraints.Positive

data class AddCartItemRequest(
    @field:Positive(message = "상품 ID는 1 이상이어야 합니다.")
    val productId: Long,

    @field:Positive(message = "상품 옵션 ID는 1 이상이어야 합니다.")
    val productOptionId: Long,

    @field:Positive(message = "수량은 1 이상이어야 합니다.")
    val quantity: Long,
)
