package zoonza.commerce.order.adapter.`in`

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.order.application.port.`in`.OrderService
import zoonza.commerce.security.CurrentMember
import zoonza.commerce.support.web.ApiResponse

@RestController
@RequestMapping("/api/orders/items/{orderItemId}")
class OrderController(
    private val orderService: OrderService,
) {
    @PostMapping("/purchase-confirmation")
    fun confirmPurchase(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable orderItemId: Long,
    ): ApiResponse<Nothing> {
        orderService.confirmPurchase(
            memberId = currentMember.memberId,
            orderItemId = orderItemId,
        )

        return ApiResponse.success()
    }
}
