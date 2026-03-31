package zoonza.commerce.order.adapter.`in`.request

import jakarta.validation.constraints.Positive

data class PlaceDirectOrderRequest(
    @field:Positive(message = "상품 ID는 1 이상이어야 합니다.")
    val productId: Long,

    @field:Positive(message = "상품 옵션 ID는 1 이상이어야 합니다.")
    val productOptionId: Long,

    @field:Positive(message = "수량은 1 이상이어야 합니다.")
    val quantity: Long,

    @field:Positive(message = "배송지 ID는 1 이상이어야 합니다.")
    val addressId: Long,
)
