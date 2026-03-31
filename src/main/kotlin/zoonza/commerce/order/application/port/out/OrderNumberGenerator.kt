package zoonza.commerce.order.application.port.out

interface OrderNumberGenerator {
    fun generate(): String
}
