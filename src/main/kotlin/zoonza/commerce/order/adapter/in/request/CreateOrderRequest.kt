package zoonza.commerce.order.adapter.`in`.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive

data class CreateOrderRequest(
    @field:Valid
    @field:NotEmpty(message = "주문상품은 최소 1개 이상이어야 합니다.")
    val items: List<CreateOrderItemRequest>,
)

data class CreateOrderItemRequest(
    @field:Positive(message = "상품 ID는 1 이상이어야 합니다.")
    val productId: Long,
    @field:Positive(message = "상품 옵션 ID는 1 이상이어야 합니다.")
    val productOptionId: Long,
    @field:Positive(message = "주문 수량은 1 이상이어야 합니다.")
    val quantity: Int,
)
