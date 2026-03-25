package zoonza.commerce.order.adapter.`in`

import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.support.web.ApiResponse
import zoonza.commerce.order.adapter.`in`.request.CreateOrderRequest
import zoonza.commerce.order.adapter.`in`.request.UpdateOrderRequest
import zoonza.commerce.order.adapter.`in`.response.CreateOrderResponse
import zoonza.commerce.order.adapter.`in`.response.OrderDetailResponse
import zoonza.commerce.order.adapter.`in`.response.OrderItemResponse
import zoonza.commerce.order.adapter.`in`.response.OrderSummaryResponse
import zoonza.commerce.order.application.dto.CreateOrderCommand
import zoonza.commerce.order.application.dto.CreateOrderItemCommand
import zoonza.commerce.order.application.dto.OrderDetail
import zoonza.commerce.order.application.dto.OrderItemDetail
import zoonza.commerce.order.application.dto.OrderSummary
import zoonza.commerce.order.application.dto.UpdateOrderCommand
import zoonza.commerce.order.application.dto.UpdateOrderItemCommand
import zoonza.commerce.order.application.port.`in`.OrderService
import zoonza.commerce.security.CurrentMember

@RestController
@RequestMapping("/api/orders")
class OrderManagementController(
    private val orderService: OrderService,
) {
    @PostMapping
    fun createOrder(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @Valid @RequestBody request: CreateOrderRequest,
    ): ApiResponse<CreateOrderResponse> {
        val createdOrder = orderService.createOrder(
            memberId = currentMember.memberId,
            command = CreateOrderCommand(
                items = request.items.map { item ->
                    CreateOrderItemCommand(
                        productId = item.productId,
                        productOptionId = item.productOptionId,
                        quantity = item.quantity,
                    )
                },
            ),
        )

        return ApiResponse.success(
            CreateOrderResponse(
                orderId = createdOrder.orderId,
                orderNumber = createdOrder.orderNumber,
                totalAmount = createdOrder.totalAmount,
            ),
        )
    }

    @GetMapping
    fun getOrders(
        @AuthenticationPrincipal currentMember: CurrentMember,
    ): ApiResponse<List<OrderSummaryResponse>> {
        val orders = orderService.getOrders(currentMember.memberId)

        return ApiResponse.success(orders.map(::toOrderSummaryResponse))
    }

    @PatchMapping("/{orderId}")
    fun updateOrder(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable orderId: Long,
        @Valid @RequestBody request: UpdateOrderRequest,
    ): ApiResponse<OrderDetailResponse> {
        val updatedOrder = orderService.updateOrder(
            memberId = currentMember.memberId,
            orderId = orderId,
            command = UpdateOrderCommand(
                items = request.items.map { item ->
                    UpdateOrderItemCommand(
                        productId = item.productId,
                        productOptionId = item.productOptionId,
                        quantity = item.quantity,
                    )
                },
            ),
        )

        return ApiResponse.success(toOrderDetailResponse(updatedOrder))
    }

    @DeleteMapping("/{orderId}")
    fun deleteOrder(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable orderId: Long,
    ): ApiResponse<Nothing> {
        orderService.deleteOrder(
            memberId = currentMember.memberId,
            orderId = orderId,
        )

        return ApiResponse.success()
    }

    @GetMapping("/{orderId}")
    fun getOrder(
        @AuthenticationPrincipal currentMember: CurrentMember,
        @PathVariable orderId: Long,
    ): ApiResponse<OrderDetailResponse> {
        val order = orderService.getOrder(
            memberId = currentMember.memberId,
            orderId = orderId,
        )

        return ApiResponse.success(toOrderDetailResponse(order))
    }

    private fun toOrderSummaryResponse(order: OrderSummary): OrderSummaryResponse {
        return OrderSummaryResponse(
            orderId = order.orderId,
            orderNumber = order.orderNumber,
            status = order.status,
            totalAmount = order.totalAmount,
            orderedAt = order.orderedAt,
        )
    }

    private fun toOrderDetailResponse(order: OrderDetail): OrderDetailResponse {
        return OrderDetailResponse(
            orderId = order.orderId,
            orderNumber = order.orderNumber,
            status = order.status,
            totalAmount = order.totalAmount,
            orderedAt = order.orderedAt,
            deliveredAt = order.deliveredAt,
            items = order.items.map(::toOrderItemResponse),
        )
    }

    private fun toOrderItemResponse(item: OrderItemDetail): OrderItemResponse {
        return OrderItemResponse(
            orderItemId = item.orderItemId,
            productId = item.productId,
            productOptionId = item.productOptionId,
            productName = item.productName,
            optionColor = item.optionColor,
            optionSize = item.optionSize,
            orderPrice = item.orderPrice,
            quantity = item.quantity,
            lineAmount = item.lineAmount,
            status = item.status,
            confirmedAt = item.confirmedAt,
        )
    }
}
