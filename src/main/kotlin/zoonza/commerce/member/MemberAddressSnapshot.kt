package zoonza.commerce.member

data class MemberAddressSnapshot(
    val id: Long,
    val label: String,
    val recipientName: String,
    val recipientPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
    val isDefault: Boolean,
)
