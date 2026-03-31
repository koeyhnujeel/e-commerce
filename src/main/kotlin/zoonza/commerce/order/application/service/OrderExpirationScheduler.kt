package zoonza.commerce.order.application.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OrderExpirationScheduler(
    private val orderExpirationService: OrderExpirationService,
) {
    @Scheduled(fixedDelayString = "\${order.expiration-worker-delay-ms:60000}")
    fun expirePendingOrders() {
        orderExpirationService.expirePendingOrders()
    }
}
