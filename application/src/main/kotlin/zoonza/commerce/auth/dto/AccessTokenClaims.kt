package zoonza.commerce.auth.dto

import zoonza.commerce.member.Role

data class AccessTokenClaims(
    val memberId: Long,
    val email: String,
    val role: Role,
)
