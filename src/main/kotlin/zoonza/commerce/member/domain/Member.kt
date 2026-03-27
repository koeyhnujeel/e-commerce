package zoonza.commerce.member.domain

import zoonza.commerce.shared.Email
import java.time.LocalDateTime

class Member(
    val id: Long = 0,
    val email: Email,
    val passwordHash: String,
    val name: String,
    val nickname: String,
    val phoneNumber: String,
    val role: Role = Role.CUSTOMER,
    val registeredAt: LocalDateTime,
    var lastLoginAt: LocalDateTime? = null,
) {
    companion object {
        fun create(
            email: Email,
            passwordHash: String,
            name: String,
            nickname: String,
            phoneNumber: String,
            role: Role = Role.CUSTOMER,
            registeredAt: LocalDateTime,
            lastLoginAt: LocalDateTime? = null,
        ): Member {
            return Member(
                email = email,
                passwordHash = passwordHash,
                name = name,
                nickname = nickname,
                phoneNumber = phoneNumber,
                role = role,
                registeredAt = registeredAt,
                lastLoginAt = lastLoginAt,
            )
        }

    }

    fun recordLogin(lastLoginAt: LocalDateTime) {
        this.lastLoginAt = lastLoginAt
    }

    fun verifyPassword(
        password: String,
        passwordEncoder: PasswordEncoder
    ): Boolean {
        return passwordEncoder.matches(password, this.passwordHash)
    }
}
