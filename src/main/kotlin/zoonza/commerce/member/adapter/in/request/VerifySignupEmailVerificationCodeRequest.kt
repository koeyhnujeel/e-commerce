package zoonza.commerce.member.adapter.`in`.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class VerifySignupEmailVerificationCodeRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String,

    @field:NotBlank(message = "인증 코드는 필수입니다.")
    @field:Pattern(
        regexp = "^\\d{3} \\d{3}$",
        message = "인증 코드는 123 456 형식이어야 합니다.",
    )
    val code: String,
)
