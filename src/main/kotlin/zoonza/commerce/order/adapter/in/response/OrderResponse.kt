package zoonza.commerce.order.adapter.`in`.response

import zoonza.commerce.order.application.dto.OrderDetailView
import zoonza.commerce.order.application.dto.OrderItemView
import zoonza.commerce.order.application.dto.OrderRecipientView
import zoonza.commerce.order.application.dto.OrderSummaryView
import java.time.LocalDateTime

data class OrderSummaryResponse(
    val orderId: Long,
    val orderNumber: String,
    val source: String,
    val status: String,
    val orderedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val totalAmount: Long,
    val itemCount: Int,
) {
    companion object {
        fun from(order: OrderSummaryView): OrderSummaryResponse {
            return OrderSummaryResponse(
                orderId = order.orderId,
                orderNumber = order.orderNumber,
                source = order.source.name,
                status = order.status.name,
                orderedAt = order.orderedAt,
                expiresAt = order.expiresAt,
                totalAmount = order.totalAmount,
                itemCount = order.itemCount,
            )
        }
    }
}

data class OrderDetailResponse(
    val orderId: Long,
    val orderNumber: String,
    val source: String,
    val status: String,
    val orderedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val totalAmount: Long,
    val recipient: OrderRecipientResponse,
    val items: List<OrderItemResponse>,
) {
    companion object {
        fun from(order: OrderDetailView): OrderDetailResponse {
            return OrderDetailResponse(
                orderId = order.orderId,
                orderNumber = order.orderNumber,
                source = order.source.name,
                status = order.status.name,
                orderedAt = order.orderedAt,
                expiresAt = order.expiresAt,
                totalAmount = order.totalAmount,
                recipient = OrderRecipientResponse.from(order.recipient),
                items = order.items.map(OrderItemResponse::from),
            )
        }
    }
}

data class OrderRecipientResponse(
    val recipientName: String,
    val recipientPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
) {
    companion object {
        fun from(recipient: OrderRecipientView): OrderRecipientResponse {
            return OrderRecipientResponse(
                recipientName = recipient.recipientName,
                recipientPhoneNumber = recipient.recipientPhoneNumber,
                zipCode = recipient.zipCode,
                baseAddress = recipient.baseAddress,
                detailAddress = recipient.detailAddress,
            )
        }
    }
}

data class OrderItemResponse(
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
        fun from(item: OrderItemView): OrderItemResponse {
            return OrderItemResponse(
                productId = item.productId,
                productOptionId = item.productOptionId,
                productName = item.productName,
                primaryImageUrl = item.primaryImageUrl,
                optionColor = item.optionColor,
                optionSize = item.optionSize,
                unitPrice = item.unitPrice,
                quantity = item.quantity,
                lineAmount = item.lineAmount,
            )
        }
    }
}
