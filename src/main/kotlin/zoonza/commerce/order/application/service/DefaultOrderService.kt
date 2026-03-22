package zoonza.commerce.order.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.order.OrderApi
import zoonza.commerce.order.ReviewablePurchase
import zoonza.commerce.order.application.dto.CreateOrderCommand
import zoonza.commerce.order.application.dto.CreateOrderItemCommand
import zoonza.commerce.order.application.dto.CreateOrderResult
import zoonza.commerce.order.application.dto.OrderDetail
import zoonza.commerce.order.application.dto.OrderItemDetail
import zoonza.commerce.order.application.dto.OrderSummary
import zoonza.commerce.order.application.port.`in`.OrderService
import zoonza.commerce.order.application.port.out.OrderNumberGenerator
import zoonza.commerce.order.application.port.out.OrderRepository
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderItem
import zoonza.commerce.order.domain.OrderItemStatus
import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.ErrorCode
import java.time.LocalDateTime

@Service
class DefaultOrderService(
    private val orderRepository: OrderRepository,
    private val catalogApi: CatalogApi,
    private val orderNumberGenerator: OrderNumberGenerator,
) : OrderApi, OrderService {
    @Transactional(readOnly = true)
    override fun findReviewablePurchase(
        memberId: Long,
        productId: Long,
    ): List<ReviewablePurchase> {
        return orderRepository.findReviewablePurchase(memberId, productId)
    }

    @Transactional
    override fun createOrder(
        memberId: Long,
        command: CreateOrderCommand,
    ): CreateOrderResult {
        val order = Order.create(
            memberId = memberId,
            orderNumber = orderNumberGenerator.generate(),
            orderedAt = LocalDateTime.now(),
            items = command.items.map(::toOrderItem),
            status = OrderStatus.CREATED,
        )
        val savedOrder = orderRepository.save(order)

        return CreateOrderResult(
            orderId = savedOrder.id,
            orderNumber = savedOrder.orderNumber,
            totalAmount = savedOrder.totalAmount.amount,
        )
    }

    @Transactional(readOnly = true)
    override fun getOrders(memberId: Long): List<OrderSummary> {
        return orderRepository.findOrders(memberId).map { order ->
            OrderSummary(
                orderId = order.id,
                orderNumber = order.orderNumber,
                status = order.status,
                totalAmount = order.totalAmount.amount,
                orderedAt = order.orderedAt,
            )
        }
    }

    @Transactional(readOnly = true)
    override fun getOrder(
        memberId: Long,
        orderId: Long,
    ): OrderDetail {
        val order = orderRepository.findOrderByIdAndMemberId(orderId, memberId)
            ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)

        return OrderDetail(
            orderId = order.id,
            orderNumber = order.orderNumber,
            status = order.status,
            totalAmount = order.totalAmount.amount,
            orderedAt = order.orderedAt,
            deliveredAt = order.deliveredAt,
            items = order.items.map { item ->
                OrderItemDetail(
                    orderItemId = item.id,
                    productId = item.productId,
                    productOptionId = item.productOptionId,
                    productName = item.productNameSnapshot,
                    optionColor = item.optionColorSnapshot,
                    optionSize = item.optionSizeSnapshot,
                    orderPrice = item.orderPrice.amount,
                    quantity = item.quantity,
                    lineAmount = item.lineAmount().amount,
                    status = item.status,
                    confirmedAt = item.confirmedAt,
                )
            },
        )
    }

    @Transactional
    override fun confirmPurchase(
        memberId: Long,
        orderItemId: Long,
    ) {
        val order = orderRepository.findOrderByMemberIdAndOrderItemId(memberId, orderItemId)
            ?: throw BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND)

        val orderItem = order.items.firstOrNull { it.id == orderItemId }
            ?: throw BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND)

        if (orderItem.status != OrderItemStatus.DELIVERED) {
            throw BusinessException(ErrorCode.ORDER_ITEM_PURCHASE_CONFIRM_NOT_ALLOWED)
        }

        val optionSnapshot = catalogApi.findProductOptionSnapshot(orderItem.productOptionId)

        order.confirmPurchase(
            orderItemId = orderItemId,
            optionColor = optionSnapshot.color,
            optionSize = optionSnapshot.size,
            confirmedAt = LocalDateTime.now(),
        )

        orderRepository.save(order)
    }

    private fun toOrderItem(command: CreateOrderItemCommand): OrderItem {
        val productSnapshot = catalogApi.findOrderProductSnapshot(
            productId = command.productId,
            productOptionId = command.productOptionId,
        )

        return OrderItem.create(
            productId = command.productId,
            productOptionId = command.productOptionId,
            productNameSnapshot = productSnapshot.productName,
            optionColorSnapshot = productSnapshot.optionColor,
            optionSizeSnapshot = productSnapshot.optionSize,
            quantity = command.quantity,
            orderPrice = productSnapshot.unitPrice,
        )
    }
}
