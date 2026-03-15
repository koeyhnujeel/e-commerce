package zoonza.commerce.adapter.`in`.member.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

private const val PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\p{Punct}]).+$"

data class SignupMemberRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Pattern(
        regexp = PASSWORD_PATTERN,
        message = "비밀번호는 영어 대문자, 소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다.",
    )
    val password: String,

    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,

    @field:NotBlank(message = "휴대폰 번호는 필수입니다.")
    val phoneNumber: String,
)
