package zoonza.commerce.member.adapter.`in`.request

import jakarta.validation.constraints.NotBlank

data class UpdateMemberAddressRequest(
    @field:NotBlank(message = "배송지명은 필수입니다.")
    val label: String,

    @field:NotBlank(message = "수령인 이름은 필수입니다.")
    val recipientName: String,

    @field:NotBlank(message = "수령인 연락처는 필수입니다.")
    val recipientPhoneNumber: String,

    @field:NotBlank(message = "우편번호는 필수입니다.")
    val zipCode: String,

    @field:NotBlank(message = "기본 주소는 필수입니다.")
    val baseAddress: String,

    val detailAddress: String = "",

    val isDefault: Boolean = false,
)
