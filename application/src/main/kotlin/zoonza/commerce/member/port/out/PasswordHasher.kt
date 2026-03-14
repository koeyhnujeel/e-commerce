package zoonza.commerce.member.port.out

interface PasswordHasher {
    fun hash(rawPassword: String): String

    fun matches(
        rawPassword: String,
        passwordHash: String,
    ): Boolean
}
