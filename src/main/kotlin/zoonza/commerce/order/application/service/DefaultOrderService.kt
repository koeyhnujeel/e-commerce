package zoonza.commerce.order.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.order.OrderApi
import zoonza.commerce.order.PaymentOrder
import zoonza.commerce.order.ReviewablePurchase
import zoonza.commerce.order.application.dto.CreateOrderCommand
import zoonza.commerce.order.application.dto.CreateOrderItemCommand
import zoonza.commerce.order.application.dto.CreateOrderResult
import zoonza.commerce.order.application.dto.OrderDetail
import zoonza.commerce.order.application.dto.OrderItemDetail
import zoonza.commerce.order.application.dto.OrderSummary
import zoonza.commerce.order.application.dto.UpdateOrderCommand
import zoonza.commerce.order.application.dto.UpdateOrderItemCommand
import zoonza.commerce.order.application.port.`in`.OrderService
import zoonza.commerce.order.application.port.out.OrderNumberGenerator
import zoonza.commerce.order.application.port.out.OrderRepository
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderItem
import zoonza.commerce.order.domain.OrderItemStatus
import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.order.OrderErrorCode
import java.time.LocalDateTime

@Service
class DefaultOrderService(
    private val orderRepository: OrderRepository,
    private val catalogApi: CatalogApi,
    private val orderNumberGenerator: OrderNumberGenerator,
) : OrderApi, OrderService {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    override fun findReviewablePurchase(
        memberId: Long,
        productId: Long,
    ): List<ReviewablePurchase> {
        return orderRepository.findReviewablePurchase(memberId, productId)
    }

    @Transactional(readOnly = true)
    override fun getPaymentOrder(
        memberId: Long,
        orderId: Long,
    ): PaymentOrder {
        val order = orderRepository.findOrderByIdAndMemberId(orderId, memberId)
            ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)

        return PaymentOrder(
            orderId = order.id,
            memberId = order.memberId,
            orderNumber = order.orderNumber,
            payable = order.status == OrderStatus.CREATED,
            totalAmount = order.totalAmount,
            productNames = order.items.map(OrderItem::productNameSnapshot),
        )
    }

    @Transactional
    override fun markPaymentPending(orderId: Long) {
        val order = orderRepository.findOrderById(orderId)
            ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)

        val previousStatus = order.status
        order.markPaymentPending()
        orderRepository.save(order)
        logOrderStatusChange(order.id, previousStatus, order.status)
    }

    @Transactional
    override fun markPaymentReady(orderId: Long) {
        val order = orderRepository.findOrderById(orderId)
            ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)

        val previousStatus = order.status
        order.markCreated()
        orderRepository.save(order)
        logOrderStatusChange(order.id, previousStatus, order.status)
    }

    @Transactional
    override fun markPaid(orderId: Long) {
        val order = orderRepository.findOrderById(orderId)
            ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)

        val previousStatus = order.status
        order.markPaid()
        orderRepository.save(order)
        logOrderStatusChange(order.id, previousStatus, order.status)
    }

    @Transactional
    override fun cancel(orderId: Long) {
        val order = orderRepository.findOrderById(orderId)
            ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)

        val previousStatus = order.status
        order.cancel()
        orderRepository.save(order)
        logOrderStatusChange(order.id, previousStatus, order.status)
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
            ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)

        return toOrderDetail(order)
    }

    @Transactional
    override fun updateOrder(
        memberId: Long,
        orderId: Long,
        command: UpdateOrderCommand,
    ): OrderDetail {
        val order = orderRepository.findOrderByIdAndMemberId(orderId, memberId)
            ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)

        if (!order.canModify()) {
            throw BusinessException(OrderErrorCode.ORDER_MODIFICATION_NOT_ALLOWED)
        }

        order.replaceItems(command.items.map(::toOrderItem))
        val updatedOrder = orderRepository.save(order)
        log.info(
            "order updated orderId={} itemCount={} totalAmount={}",
            updatedOrder.id,
            updatedOrder.items.size,
            updatedOrder.totalAmount.amount,
        )

        return toOrderDetail(updatedOrder)
    }

    @Transactional
    override fun deleteOrder(
        memberId: Long,
        orderId: Long,
    ) {
        val order = orderRepository.findOrderByIdAndMemberId(orderId, memberId)
            ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)

        if (!order.canDelete()) {
            throw BusinessException(OrderErrorCode.ORDER_DELETION_NOT_ALLOWED)
        }

        val previousStatus = order.status
        order.delete(LocalDateTime.now())
        orderRepository.save(order)
        logOrderStatusChange(order.id, previousStatus, order.status)
        log.info(
            "order deleted orderId={} deletedAt={}",
            order.id,
            order.deletedAt,
        )
    }

    private fun toOrderDetail(order: Order): OrderDetail {
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
            ?: throw BusinessException(OrderErrorCode.ORDER_ITEM_NOT_FOUND)

        val orderItem = order.items.firstOrNull { it.id == orderItemId }
            ?: throw BusinessException(OrderErrorCode.ORDER_ITEM_NOT_FOUND)

        if (orderItem.status != OrderItemStatus.DELIVERED) {
            throw BusinessException(OrderErrorCode.ORDER_ITEM_PURCHASE_CONFIRM_NOT_ALLOWED)
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
        return toOrderItem(
            productId = command.productId,
            productOptionId = command.productOptionId,
            quantity = command.quantity,
        )
    }

    private fun toOrderItem(command: UpdateOrderItemCommand): OrderItem {
        return toOrderItem(
            productId = command.productId,
            productOptionId = command.productOptionId,
            quantity = command.quantity,
        )
    }

    private fun toOrderItem(
        productId: Long,
        productOptionId: Long,
        quantity: Int,
    ): OrderItem {
        val productSnapshot = catalogApi.findOrderProductSnapshot(
            productId = productId,
            productOptionId = productOptionId,
        )

        return OrderItem.create(
            productId = productId,
            productOptionId = productOptionId,
            productNameSnapshot = productSnapshot.productName,
            optionColorSnapshot = productSnapshot.option.color,
            optionSizeSnapshot = productSnapshot.option.size,
            quantity = quantity,
            orderPrice = productSnapshot.unitPrice,
        )
    }

    private fun logOrderStatusChange(
        orderId: Long,
        from: OrderStatus,
        to: OrderStatus,
    ) {
        log.info("order status changed orderId={} from={} to={}", orderId, from, to)
    }
}
