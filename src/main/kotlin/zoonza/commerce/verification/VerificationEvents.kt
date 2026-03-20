package zoonza.commerce.verification

import zoonza.commerce.shared.Email

data class VerificationCodeCreated(
    val email: Email,
    val code: String
)