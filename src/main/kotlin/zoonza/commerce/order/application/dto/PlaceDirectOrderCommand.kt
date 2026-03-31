package zoonza.commerce.order.application.dto

data class PlaceDirectOrderCommand(
    val productId: Long,
    val productOptionId: Long,
    val quantity: Long,
    val addressId: Long,
) {
    companion object {
        fun of(
            productId: Long,
            productOptionId: Long,
            quantity: Long,
            addressId: Long,
        ): PlaceDirectOrderCommand {
            return PlaceDirectOrderCommand(
                productId = productId,
                productOptionId = productOptionId,
                quantity = quantity,
                addressId = addressId,
            )
        }
    }
}
