package zoonza.commerce.adapter.`in`.member.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

private const val PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\p{Punct}]).+$"

data class SignupMemberRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Pattern(
        regexp = PASSWORD_PATTERN,
        message = "비밀번호는 영어 대문자, 소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다.",
    )
    val password: String,

    @field:NotBlank
    val name: String,

    @field:NotBlank
    val phoneNumber: String,
)
