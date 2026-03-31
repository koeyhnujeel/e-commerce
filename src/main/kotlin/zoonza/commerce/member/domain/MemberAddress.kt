package zoonza.commerce.member.domain

class MemberAddress(
    val id: Long = 0L,
    var label: String,
    var recipientName: String,
    var recipientPhoneNumber: String,
    var zipCode: String,
    var baseAddress: String,
    var detailAddress: String,
    var isDefault: Boolean = false,
) {
    companion object {
        fun create(
            label: String,
            recipientName: String,
            recipientPhoneNumber: String,
            zipCode: String,
            baseAddress: String,
            detailAddress: String,
            isDefault: Boolean = false,
        ): MemberAddress {
            return MemberAddress(
                label = label.trim(),
                recipientName = recipientName.trim(),
                recipientPhoneNumber = recipientPhoneNumber.trim(),
                zipCode = zipCode.trim(),
                baseAddress = baseAddress.trim(),
                detailAddress = detailAddress.trim(),
                isDefault = isDefault,
            ).also { it.validate() }
        }
    }

    fun update(
        label: String,
        recipientName: String,
        recipientPhoneNumber: String,
        zipCode: String,
        baseAddress: String,
        detailAddress: String,
        isDefault: Boolean,
    ) {
        this.label = label.trim()
        this.recipientName = recipientName.trim()
        this.recipientPhoneNumber = recipientPhoneNumber.trim()
        this.zipCode = zipCode.trim()
        this.baseAddress = baseAddress.trim()
        this.detailAddress = detailAddress.trim()
        this.isDefault = isDefault
        validate()
    }

    fun markDefault() {
        isDefault = true
    }

    fun unmarkDefault() {
        isDefault = false
    }

    private fun validate() {
        require(label.isNotBlank()) { "배송지명은 비어 있을 수 없습니다." }
        require(recipientName.isNotBlank()) { "수령인 이름은 비어 있을 수 없습니다." }
        require(recipientPhoneNumber.isNotBlank()) { "수령인 연락처는 비어 있을 수 없습니다." }
        require(zipCode.isNotBlank()) { "우편번호는 비어 있을 수 없습니다." }
        require(baseAddress.isNotBlank()) { "기본 주소는 비어 있을 수 없습니다." }
    }
}
