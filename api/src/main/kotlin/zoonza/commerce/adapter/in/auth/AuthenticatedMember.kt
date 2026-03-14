package zoonza.commerce.adapter.`in`.auth

import zoonza.commerce.member.Role

data class AuthenticatedMember(
    val memberId: Long,
    val email: String,
    val role: Role,
)
