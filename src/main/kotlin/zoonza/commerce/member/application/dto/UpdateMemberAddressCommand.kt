package zoonza.commerce.member.application.dto

data class UpdateMemberAddressCommand(
    val label: String,
    val recipientName: String,
    val recipientPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
    val isDefault: Boolean,
) {
    companion object {
        fun of(
            label: String,
            recipientName: String,
            recipientPhoneNumber: String,
            zipCode: String,
            baseAddress: String,
            detailAddress: String,
            isDefault: Boolean,
        ): UpdateMemberAddressCommand {
            return UpdateMemberAddressCommand(
                label = label,
                recipientName = recipientName,
                recipientPhoneNumber = recipientPhoneNumber,
                zipCode = zipCode,
                baseAddress = baseAddress,
                detailAddress = detailAddress,
                isDefault = isDefault,
            )
        }
    }
}
