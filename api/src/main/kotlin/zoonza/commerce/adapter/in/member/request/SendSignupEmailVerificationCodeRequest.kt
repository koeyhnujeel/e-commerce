package zoonza.commerce.adapter.`in`.member.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SendSignupEmailVerificationCodeRequest(
    @field:NotBlank
    @field:Email
    val email: String,
)
