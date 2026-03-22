package zoonza.commerce.payment.domain

enum class PaymentMethod {
    CARD,
    EASY_PAY,
    TRANSFER,
    VIRTUAL_ACCOUNT,
    MOBILE_PHONE,
    UNKNOWN,
    ;

    companion object {
        fun fromProvider(method: String?): PaymentMethod {
            return when (method?.trim()?.uppercase()) {
                "CARD", "카드" -> CARD
                "EASY_PAY", "간편결제" -> EASY_PAY
                "TRANSFER", "계좌이체" -> TRANSFER
                "VIRTUAL_ACCOUNT", "가상계좌" -> VIRTUAL_ACCOUNT
                "MOBILE_PHONE", "휴대폰" -> MOBILE_PHONE
                else -> UNKNOWN
            }
        }
    }
}
