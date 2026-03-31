package zoonza.commerce.order.application.dto

data class PlaceCartOrderCommand(
    val productOptionIds: Set<Long>,
    val addressId: Long,
) {
    companion object {
        fun of(
            productOptionIds: Set<Long>,
            addressId: Long,
        ): PlaceCartOrderCommand {
            return PlaceCartOrderCommand(
                productOptionIds = productOptionIds,
                addressId = addressId,
            )
        }
    }
}
