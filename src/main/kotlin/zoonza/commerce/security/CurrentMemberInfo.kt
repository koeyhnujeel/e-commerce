package zoonza.commerce.security

data class CurrentMemberInfo(
    val memberId: Long,
    val email: String,
    val role: String,
)
