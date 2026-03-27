package zoonza.commerce.support.fixture

import zoonza.commerce.catalog.adapter.out.persistence.product.ProductJpaEntity
import zoonza.commerce.catalog.adapter.out.persistence.product.ProductOptionJpaEntity
import zoonza.commerce.order.adapter.out.persistence.OrderJpaEntity
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderItem
import zoonza.commerce.order.domain.OrderStatus
import zoonza.commerce.shared.Money
import java.time.LocalDateTime

object OrderFixture {
    fun create(
        memberId: Long,
        product: ProductJpaEntity,
        orderNumber: String,
        orderedAt: LocalDateTime,
        status: OrderStatus = OrderStatus.CREATED,
        deliveredAt: LocalDateTime? = null,
        quantity: Int = 1,
        productOption: ProductOptionJpaEntity = product.options.first(),
        orderPrice: Money = product.basePrice,
    ): OrderJpaEntity {
        return OrderJpaEntity.from(
            Order.create(
                memberId = memberId,
                orderNumber = orderNumber,
                status = status,
                orderedAt = orderedAt,
                deliveredAt = deliveredAt,
                items =
                    listOf(
                        OrderItem.create(
                            productId = product.id,
                            productOptionId = productOption.id,
                            productNameSnapshot = product.name,
                            optionColorSnapshot = productOption.color,
                            optionSizeSnapshot = productOption.size,
                            quantity = quantity,
                            orderPrice = orderPrice,
                        ),
                    ),
            ),
        )
    }

    fun createDelivered(
        memberId: Long,
        product: ProductJpaEntity,
        orderNumber: String,
        deliveredAt: LocalDateTime,
        quantity: Int = 1,
        productOption: ProductOptionJpaEntity = product.options.first(),
        orderPrice: Money = product.basePrice,
    ): OrderJpaEntity {
        return create(
            memberId = memberId,
            product = product,
            orderNumber = orderNumber,
            orderedAt = deliveredAt.minusDays(2),
            status = OrderStatus.DELIVERED,
            deliveredAt = deliveredAt,
            quantity = quantity,
            productOption = productOption,
            orderPrice = orderPrice,
        )
    }

    fun createPurchaseConfirmed(
        memberId: Long,
        product: ProductJpaEntity,
        orderNumber: String,
        deliveredAt: LocalDateTime,
        confirmedAt: LocalDateTime,
        quantity: Int = 1,
        productOption: ProductOptionJpaEntity = product.options.first(),
        orderPrice: Money = product.basePrice,
    ): OrderJpaEntity {
        val order =
            createDelivered(
                memberId = memberId,
                product = product,
                orderNumber = orderNumber,
                deliveredAt = deliveredAt,
                quantity = quantity,
                productOption = productOption,
                orderPrice = orderPrice,
            )

        val orderDomain = order.toDomain()
        orderDomain.confirmPurchase(
            orderItemId = orderDomain.items.single().id,
            optionColor = productOption.color,
            optionSize = productOption.size,
            confirmedAt = confirmedAt,
        )

        return OrderJpaEntity.from(orderDomain)
    }
}
