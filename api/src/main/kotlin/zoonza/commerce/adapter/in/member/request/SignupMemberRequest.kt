package zoonza.commerce.adapter.`in`.member.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SignupMemberRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    val password: String,

    @field:NotBlank
    val name: String,

    @field:NotBlank
    val phoneNumber: String,
)
