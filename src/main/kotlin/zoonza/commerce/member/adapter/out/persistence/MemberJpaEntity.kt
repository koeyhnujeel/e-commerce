package zoonza.commerce.member.adapter.out.persistence

import jakarta.persistence.*
import zoonza.commerce.member.domain.Member
import zoonza.commerce.member.domain.Role
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
class MemberJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Embedded
    val email: Email = Email("test@example.com"),

    @Column(nullable = false)
    val passwordHash: String = "",

    @Column(nullable = false)
    val name: String = "",

    @Column(unique = true, nullable = false)
    val nickname: String = "",

    @Column(unique = true, nullable = false)
    val phoneNumber: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER'")
    val role: Role = Role.CUSTOMER,

    @Column(nullable = false)
    val registeredAt: LocalDateTime = LocalDateTime.MIN,

    @Column
    val lastLoginAt: LocalDateTime? = null,
) {
    fun toDomain(): Member {
        return Member(
            id = id,
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

    companion object {
        fun from(member: Member): MemberJpaEntity {
            return MemberJpaEntity(
                id = member.id,
                email = member.email,
                passwordHash = member.passwordHash,
                name = member.name,
                nickname = member.nickname,
                phoneNumber = member.phoneNumber,
                role = member.role,
                registeredAt = member.registeredAt,
                lastLoginAt = member.lastLoginAt,
            )
        }
    }
}
