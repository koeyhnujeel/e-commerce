package zoonza.commerce.payment.application.service

import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import zoonza.commerce.order.OrderCanceled
import zoonza.commerce.order.OrderExpired

@Service
class PaymentOrderEventHandler(
    private val paymentService: DefaultPaymentService,
) {
    @ApplicationModuleListener
    fun handle(event: OrderCanceled) {
        paymentService.closeByOrderCancellation(event)
    }

    @ApplicationModuleListener
    fun handle(event: OrderExpired) {
        paymentService.closeByOrderExpiration(event)
    }
}
