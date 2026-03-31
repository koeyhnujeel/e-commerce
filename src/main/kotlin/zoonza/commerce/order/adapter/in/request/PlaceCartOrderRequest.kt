package zoonza.commerce.order.adapter.`in`.request

import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class PlaceCartOrderRequest(
    @field:Size(min = 1, message = "주문할 상품 옵션은 최소 1개 이상이어야 합니다.")
    val productOptionIds: Set<Long>,

    @field:Positive(message = "배송지 ID는 1 이상이어야 합니다.")
    val addressId: Long,
)
