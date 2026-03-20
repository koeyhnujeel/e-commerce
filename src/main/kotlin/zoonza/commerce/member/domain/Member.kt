package zoonza.commerce.member.domain

import jakarta.persistence.*
import zoonza.commerce.shared.Email
import java.time.LocalDateTime

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_email",
            columnNames = ["email"],
        ),
    ],
)
class Member private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Embedded
    val email: Email,

    @Column(nullable = false)
    val passwordHash: String,

    @Column(nullable = false)
    val name: String,

    @Column(unique = true, nullable = false)
    val nickname: String,

    @Column(unique = true, nullable = false)
    val phoneNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER'")
    val role: Role = Role.CUSTOMER,

    @Column(nullable = false)
    val registeredAt: LocalDateTime,

    @Column
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
