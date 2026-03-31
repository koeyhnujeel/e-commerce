package zoonza.commerce.order.application.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.cart.CartApi
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.inventory.InventoryApi
import zoonza.commerce.member.MemberApi
import zoonza.commerce.order.OrderApi
import zoonza.commerce.order.OrderCreated
import zoonza.commerce.order.OrderErrorCode
import zoonza.commerce.order.OrderPaid
import zoonza.commerce.order.PendingPaymentOrder
import zoonza.commerce.order.application.dto.OrderDetailView
import zoonza.commerce.order.application.dto.OrderItemView
import zoonza.commerce.order.application.dto.OrderRecipientView
import zoonza.commerce.order.application.dto.OrderSummaryView
import zoonza.commerce.order.application.dto.PlaceCartOrderCommand
import zoonza.commerce.order.application.dto.PlaceDirectOrderCommand
import zoonza.commerce.order.application.dto.PlaceOrderResult
import zoonza.commerce.order.application.port.`in`.OrderService
import zoonza.commerce.order.application.port.out.OrderNumberGenerator
import zoonza.commerce.order.application.port.out.OrderRepository
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderItem
import zoonza.commerce.order.domain.OrderRecipient
import zoonza.commerce.order.domain.OrderSource
import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

@Service
class DefaultOrderService(
    private val orderRepository: OrderRepository,
    private val orderNumberGenerator: OrderNumberGenerator,
    private val catalogApi: CatalogApi,
    private val inventoryApi: InventoryApi,
    private val memberApi: MemberApi,
    private val cartApi: CartApi,
    private val eventPublisher: ApplicationEventPublisher,
) : OrderService, OrderApi {
    companion object {
        private const val PENDING_PAYMENT_EXPIRES_IN_MINUTES = 10L
    }

    @Transactional
    override fun placeDirectOrder(
        memberId: Long,
        command: PlaceDirectOrderCommand,
    ): PlaceOrderResult {
        catalogApi.validateAvailableProductOption(command.productId, command.productOptionId)

        val address = memberApi.findShippingAddress(memberId, command.addressId)
        val optionSummary = catalogApi.getProductOptionSummaries(setOf(command.productOptionId))[command.productOptionId]
            ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)

        val now = LocalDateTime.now()
        val expiresAt = now.plusMinutes(PENDING_PAYMENT_EXPIRES_IN_MINUTES)
        val orderNumber = orderNumberGenerator.generate()

        inventoryApi.reserve(
            productOptionId = command.productOptionId,
            orderNumber = orderNumber,
            quantity = command.quantity,
            reservedAt = now,
            expiresAt = expiresAt,
        )

        val order =
            Order.create(
                memberId = memberId,
                orderNumber = orderNumber,
                source = OrderSource.DIRECT_BUY,
                orderedAt = now,
                expiresAt = expiresAt,
                recipient = toRecipient(address),
                items =
                    listOf(
                        OrderItem(
                            productId = command.productId,
                            productOptionId = command.productOptionId,
                            productName = optionSummary.productName,
                            primaryImageUrl = optionSummary.primaryImageUrl,
                            optionColor = optionSummary.color,
                            optionSize = optionSummary.size,
                            unitPrice = Money(optionSummary.basePrice + optionSummary.additionalPrice),
                            quantity = command.quantity,
                        ),
                    ),
            )

        return saveCreatedOrder(order)
    }

    @Transactional
    override fun placeCartOrder(
        memberId: Long,
        command: PlaceCartOrderCommand,
    ): PlaceOrderResult {
        val selectedItems = cartApi.getSelectedItems(memberId, command.productOptionIds)
        val optionSummaries = catalogApi.getProductOptionSummaries(command.productOptionIds)
        val availableQuantities = inventoryApi.getAvailableQuantities(command.productOptionIds)
        val address = memberApi.findShippingAddress(memberId, command.addressId)

        if (selectedItems.size != command.productOptionIds.size || optionSummaries.size != command.productOptionIds.size) {
            throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)
        }

        val now = LocalDateTime.now()
        val expiresAt = now.plusMinutes(PENDING_PAYMENT_EXPIRES_IN_MINUTES)
        val orderNumber = orderNumberGenerator.generate()

        val orderItems =
            selectedItems.map { selectedItem ->
                val optionSummary = optionSummaries[selectedItem.productOptionId]
                    ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)
                val availableQuantity = availableQuantities[selectedItem.productOptionId] ?: 0L

                if (!optionSummary.availableForSale || availableQuantity < selectedItem.quantity) {
                    throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)
                }

                inventoryApi.reserve(
                    productOptionId = selectedItem.productOptionId,
                    orderNumber = orderNumber,
                    quantity = selectedItem.quantity,
                    reservedAt = now,
                    expiresAt = expiresAt,
                )

                OrderItem(
                    productId = selectedItem.productId,
                    productOptionId = selectedItem.productOptionId,
                    productName = optionSummary.productName,
                    primaryImageUrl = optionSummary.primaryImageUrl,
                    optionColor = optionSummary.color,
                    optionSize = optionSummary.size,
                    unitPrice = Money(optionSummary.basePrice + optionSummary.additionalPrice),
                    quantity = selectedItem.quantity,
                )
            }

        val savedOrder =
            orderRepository.save(
                Order.create(
                    memberId = memberId,
                    orderNumber = orderNumber,
                    source = OrderSource.CART,
                    orderedAt = now,
                    expiresAt = expiresAt,
                    recipient = toRecipient(address),
                    items = orderItems,
                ),
            )

        cartApi.removeItems(memberId, command.productOptionIds)
        eventPublisher.publishEvent(OrderCreated(savedOrder.id, savedOrder.orderNumber, savedOrder.memberId))

        return PlaceOrderResult.of(
            orderId = savedOrder.id,
            orderNumber = savedOrder.orderNumber,
            status = savedOrder.status,
            expiresAt = savedOrder.expiresAt,
            totalAmount = savedOrder.totalAmount.amount.longValueExact(),
        )
    }

    @Transactional(readOnly = true)
    override fun getMyOrders(memberId: Long): List<OrderSummaryView> {
        return orderRepository.findAllByMemberId(memberId).map(OrderSummaryView::from)
    }

    @Transactional(readOnly = true)
    override fun getMyOrder(
        memberId: Long,
        orderId: Long,
    ): OrderDetailView {
        val order = orderRepository.findByIdAndMemberId(orderId, memberId)
            ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)
        return OrderDetailView.from(order)
    }

    @Transactional
    override fun cancel(
        memberId: Long,
        orderId: Long,
    ) {
        val order = orderRepository.findByIdAndMemberId(orderId, memberId)
            ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)

        try {
            order.cancel(LocalDateTime.now())
        } catch (_: IllegalArgumentException) {
            throw BusinessException(OrderErrorCode.ORDER_CANCELLATION_NOT_ALLOWED)
        }

        order.items.forEach { item ->
            inventoryApi.releaseReservation(item.productOptionId, order.orderNumber, order.canceledAt!!)
        }
        orderRepository.save(order)
        eventPublisher.publishEvent(zoonza.commerce.order.OrderCanceled(order.id, order.orderNumber, order.memberId))
    }

    @Transactional(readOnly = true)
    override fun findPendingPaymentTarget(orderId: Long): PendingPaymentOrder {
        val order = orderRepository.findById(orderId)
            ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)

        if (order.status != OrderStatus.PENDING_PAYMENT) {
            throw BusinessException(OrderErrorCode.ORDER_PAYMENT_NOT_ALLOWED)
        }

        return PendingPaymentOrder(
            orderId = order.id,
            orderNumber = order.orderNumber,
            memberId = order.memberId,
            totalAmount = order.totalAmount.amount.longValueExact(),
            expiresAt = order.expiresAt,
            productNames = order.items.map(OrderItem::productName),
        )
    }

    @Transactional
    override fun markPaid(orderId: Long) {
        val order = orderRepository.findById(orderId)
            ?: throw BusinessException(OrderErrorCode.ORDER_NOT_FOUND)

        val paidAt = LocalDateTime.now()
        try {
            order.markPaid(paidAt)
        } catch (_: IllegalArgumentException) {
            throw BusinessException(OrderErrorCode.ORDER_PAYMENT_NOT_ALLOWED)
        }

        order.items.forEach { item ->
            inventoryApi.confirmReservation(item.productOptionId, order.orderNumber, paidAt)
        }
        orderRepository.save(order)
        eventPublisher.publishEvent(OrderPaid(order.id, order.orderNumber, order.memberId))
    }

    private fun saveCreatedOrder(order: Order): PlaceOrderResult {
        val savedOrder = orderRepository.save(order)
        eventPublisher.publishEvent(OrderCreated(savedOrder.id, savedOrder.orderNumber, savedOrder.memberId))

        return PlaceOrderResult.of(
            orderId = savedOrder.id,
            orderNumber = savedOrder.orderNumber,
            status = savedOrder.status,
            expiresAt = savedOrder.expiresAt,
            totalAmount = savedOrder.totalAmount.amount.longValueExact(),
        )
    }

    private fun toRecipient(address: zoonza.commerce.member.MemberAddressSnapshot): OrderRecipient {
        return OrderRecipient(
            recipientName = address.recipientName,
            recipientPhoneNumber = address.recipientPhoneNumber,
            zipCode = address.zipCode,
            baseAddress = address.baseAddress,
            detailAddress = address.detailAddress,
        )
    }
}
