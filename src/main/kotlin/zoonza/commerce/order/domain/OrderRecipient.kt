package zoonza.commerce.order.domain

class OrderRecipient(
    val recipientName: String,
    val recipientPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
) {
    init {
        require(recipientName.isNotBlank()) { "수령인 이름은 비어 있을 수 없습니다." }
        require(recipientPhoneNumber.isNotBlank()) { "수령인 연락처는 비어 있을 수 없습니다." }
        require(zipCode.isNotBlank()) { "우편번호는 비어 있을 수 없습니다." }
        require(baseAddress.isNotBlank()) { "기본 주소는 비어 있을 수 없습니다." }
    }
}
