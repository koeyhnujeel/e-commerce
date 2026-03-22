package zoonza.commerce.order

data class ReviewablePurchase(
    val orderItemId: Long,
    val optionColor: String,
    val optionSize: String,
)
