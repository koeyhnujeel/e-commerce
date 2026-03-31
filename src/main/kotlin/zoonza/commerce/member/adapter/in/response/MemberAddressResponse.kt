package zoonza.commerce.member.adapter.`in`.response

import zoonza.commerce.member.MemberAddressSnapshot

data class MemberAddressResponse(
    val id: Long,
    val label: String,
    val recipientName: String,
    val recipientPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
    val isDefault: Boolean,
) {
    companion object {
        fun from(address: MemberAddressSnapshot): MemberAddressResponse {
            return MemberAddressResponse(
                id = address.id,
                label = address.label,
                recipientName = address.recipientName,
                recipientPhoneNumber = address.recipientPhoneNumber,
                zipCode = address.zipCode,
                baseAddress = address.baseAddress,
                detailAddress = address.detailAddress,
                isDefault = address.isDefault,
            )
        }
    }
}
