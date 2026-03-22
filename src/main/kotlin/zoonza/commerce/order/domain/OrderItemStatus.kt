package zoonza.commerce.order.domain

enum class OrderItemStatus {
    CREATED,
    PAYMENT_PENDING,
    PAID,
    DELIVERED,
    PURCHASE_CONFIRMED,
    CANCELED,
}
