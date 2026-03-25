package zoonza.commerce.security

data class CurrentMember(
    val memberId: Long,
    val email: String,
    val role: String,
)
