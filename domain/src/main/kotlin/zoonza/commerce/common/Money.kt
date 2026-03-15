package zoonza.commerce.common

data class Money(
    val amount: Long,
) {
    init {
        require(amount >= 0) {
            "금액은 0 이상이어야 합니다."
        }
    }
}
