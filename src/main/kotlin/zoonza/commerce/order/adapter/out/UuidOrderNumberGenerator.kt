package zoonza.commerce.order.adapter.out

import org.springframework.stereotype.Component
import zoonza.commerce.order.application.port.out.OrderNumberGenerator
import java.util.UUID

@Component
class UuidOrderNumberGenerator : OrderNumberGenerator {
    override fun generate(): String {
        return "ORD-${UUID.randomUUID().toString().replace("-", "").uppercase()}"
    }
}
