package zoonza.commerce.security.internal

data class AuthMember(
    val memberId: Long,
    val email: String,
    val role: String
)
