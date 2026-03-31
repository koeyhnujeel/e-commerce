package zoonza.commerce.order.application.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.inventory.InventoryApi
import zoonza.commerce.order.OrderErrorCode
import zoonza.commerce.order.OrderExpired
import zoonza.commerce.order.application.port.out.OrderRepository
import zoonza.commerce.shared.BusinessException
import java.time.LocalDateTime

@Service
class OrderExpirationService(
    private val orderRepository: OrderRepository,
    private val inventoryApi: InventoryApi,
    private val eventPublisher: ApplicationEventPublisher,
) {
    companion object {
        private const val BATCH_SIZE = 100
    }

    @Transactional
    fun expirePendingOrders(now: LocalDateTime = LocalDateTime.now()): Int {
        var expiredCount = 0

        while (true) {
            val expiredOrders = orderRepository.findExpiredPendingOrders(now, BATCH_SIZE)
            if (expiredOrders.isEmpty()) {
                return expiredCount
            }

            expiredOrders.forEach { order ->
                try {
                    order.expire(now)
                } catch (_: IllegalArgumentException) {
                    throw BusinessException(OrderErrorCode.ORDER_EXPIRATION_NOT_ALLOWED)
                }

                order.items.forEach { item ->
                    inventoryApi.expireReservation(item.productOptionId, order.orderNumber, now)
                }
                orderRepository.save(order)
                eventPublisher.publishEvent(OrderExpired(order.id, order.orderNumber, order.memberId))
                expiredCount++
            }
        }
    }
}
