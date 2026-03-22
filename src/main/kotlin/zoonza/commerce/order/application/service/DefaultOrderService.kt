package zoonza.commerce.order.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.order.OrderApi
import zoonza.commerce.order.ReviewablePurchase
import zoonza.commerce.order.application.port.`in`.OrderService
import zoonza.commerce.order.application.port.out.OrderRepository
import zoonza.commerce.order.domain.OrderItemStatus
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.ErrorCode
import java.time.LocalDateTime

@Service
class DefaultOrderService(
    private val orderRepository: OrderRepository,
    private val catalogApi: CatalogApi,
) : OrderApi, OrderService {
    @Transactional(readOnly = true)
    override fun findReviewablePurchase(
        memberId: Long,
        productId: Long,
    ): List<ReviewablePurchase> {
        return orderRepository.findReviewablePurchase(memberId, productId)
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
}
