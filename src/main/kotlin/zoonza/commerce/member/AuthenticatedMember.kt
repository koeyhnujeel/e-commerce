package zoonza.commerce.member

import zoonza.commerce.shared.Email

data class AuthenticatedMember(
    val id: Long,
    val email: Email,
    val role: String
)
