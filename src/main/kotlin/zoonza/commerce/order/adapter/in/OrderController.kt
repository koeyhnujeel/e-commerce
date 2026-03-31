package zoonza.commerce.order.adapter.`in`

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import zoonza.commerce.order.adapter.`in`.request.PlaceCartOrderRequest
import zoonza.commerce.order.adapter.`in`.request.PlaceDirectOrderRequest
import zoonza.commerce.order.adapter.`in`.response.OrderDetailResponse
import zoonza.commerce.order.adapter.`in`.response.OrderSummaryResponse
import zoonza.commerce.order.adapter.`in`.response.PlaceOrderResponse
import zoonza.commerce.order.application.dto.PlaceCartOrderCommand
import zoonza.commerce.order.application.dto.PlaceDirectOrderCommand
import zoonza.commerce.order.application.port.`in`.OrderService
import zoonza.commerce.security.CurrentMember
import zoonza.commerce.support.web.ApiResponse

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @PostMapping("/direct")
    fun placeDirectOrder(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @Valid @RequestBody request: PlaceDirectOrderRequest,
    ): ApiResponse<PlaceOrderResponse> {
        val result =
            orderService.placeDirectOrder(
                memberId = currentMember.memberId,
                command = PlaceDirectOrderCommand.of(
                    productId = request.productId,
                    productOptionId = request.productOptionId,
                    quantity = request.quantity,
                    addressId = request.addressId,
                ),
            )

        return ApiResponse.success(PlaceOrderResponse.from(result))
    }

    @PostMapping("/from-cart")
    fun placeCartOrder(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @Valid @RequestBody request: PlaceCartOrderRequest,
    ): ApiResponse<PlaceOrderResponse> {
        val result =
            orderService.placeCartOrder(
                memberId = currentMember.memberId,
                command = PlaceCartOrderCommand.of(
                    productOptionIds = request.productOptionIds,
                    addressId = request.addressId,
                ),
            )

        return ApiResponse.success(PlaceOrderResponse.from(result))
    }

    @GetMapping
    fun getMyOrders(
        @AuthenticationPrincipal currentMember: CurrentMember,
    ): ApiResponse<List<OrderSummaryResponse>> {
        return ApiResponse.success(
            orderService.getMyOrders(currentMember.memberId).map(OrderSummaryResponse::from),
        )
    }

    @GetMapping("/{orderId}")
    fun getMyOrder(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable orderId: Long,
    ): ApiResponse<OrderDetailResponse> {
        return ApiResponse.success(
            OrderDetailResponse.from(orderService.getMyOrder(currentMember.memberId, orderId)),
        )
    }

    @PostMapping("/{orderId}/cancel")
    fun cancel(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable orderId: Long,
    ): ApiResponse<Nothing> {
        orderService.cancel(currentMember.memberId, orderId)
        return ApiResponse.success()
    }
}
