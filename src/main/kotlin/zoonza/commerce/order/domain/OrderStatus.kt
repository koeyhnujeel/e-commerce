package zoonza.commerce.order.domain

enum class OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAID,
    DELIVERED,
    CANCELED,
}
