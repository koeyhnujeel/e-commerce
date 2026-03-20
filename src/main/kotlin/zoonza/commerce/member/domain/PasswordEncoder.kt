package zoonza.commerce.member.domain

interface PasswordEncoder {
    fun encode(rawPassword: String): String

    fun matches(
        rawPassword: String,
        passwordHash: String,
    ): Boolean
}