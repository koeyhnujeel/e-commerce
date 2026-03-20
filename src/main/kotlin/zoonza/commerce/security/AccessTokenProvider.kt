package zoonza.commerce.security

interface AccessTokenProvider {
    fun issue(memberId: Long, email: String, role: String): String
}