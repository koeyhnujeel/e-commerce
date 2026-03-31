package zoonza.commerce.order.application.dto

import zoonza.commerce.order.domain.OrderSource
import zoonza.commerce.order.domain.Order
import zoonza.commerce.order.domain.OrderItem
import zoonza.commerce.order.domain.OrderRecipient
import zoonza.commerce.order.domain.OrderStatus
import java.time.LocalDateTime

data class OrderSummaryView(
    val orderId: Long,
    val orderNumber: String,
    val source: OrderSource,
    val status: OrderStatus,
    val orderedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val totalAmount: Long,
    val itemCount: Int,
) {
    companion object {
        fun from(order: Order): OrderSummaryView {
            return OrderSummaryView(
                orderId = order.id,
                orderNumber = order.orderNumber,
                source = order.source,
                status = order.status,
                orderedAt = order.orderedAt,
                expiresAt = order.expiresAt,
                totalAmount = order.totalAmount.amount.longValueExact(),
                itemCount = order.items.size,
            )
        }
    }
}

data class OrderDetailView(
    val orderId: Long,
    val orderNumber: String,
    val source: OrderSource,
    val status: OrderStatus,
    val orderedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val totalAmount: Long,
    val recipient: OrderRecipientView,
    val items: List<OrderItemView>,
) {
    companion object {
        fun from(order: Order): OrderDetailView {
            return OrderDetailView(
                orderId = order.id,
                orderNumber = order.orderNumber,
                source = order.source,
                status = order.status,
                orderedAt = order.orderedAt,
                expiresAt = order.expiresAt,
                totalAmount = order.totalAmount.amount.longValueExact(),
                recipient = OrderRecipientView.from(order.recipient),
                items = order.items.map(OrderItemView::from),
            )
        }
    }
}

data class OrderRecipientView(
    val recipientName: String,
    val recipientPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
) {
    companion object {
        fun from(recipient: OrderRecipient): OrderRecipientView {
            return OrderRecipientView(
                recipientName = recipient.recipientName,
                recipientPhoneNumber = recipient.recipientPhoneNumber,
                zipCode = recipient.zipCode,
                baseAddress = recipient.baseAddress,
                detailAddress = recipient.detailAddress,
            )
        }
    }
}

data class OrderItemView(
    val productId: Long,
    val productOptionId: Long,
    val productName: String,
    val primaryImageUrl: String?,
    val optionColor: String,
    val optionSize: String,
    val unitPrice: Long,
    val quantity: Long,
    val lineAmount: Long,
) {
    companion object {
        fun from(item: OrderItem): OrderItemView {
            return OrderItemView(
                productId = item.productId,
                productOptionId = item.productOptionId,
                productName = item.productName,
                primaryImageUrl = item.primaryImageUrl,
                optionColor = item.optionColor,
                optionSize = item.optionSize,
                unitPrice = item.unitPrice.amount.longValueExact(),
                quantity = item.quantity,
                lineAmount = item.lineAmount().amount.longValueExact(),
            )
        }
    }
}
