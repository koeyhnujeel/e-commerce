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

    @Column(name = "email", nullable = false)
    val email: String = "",

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

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "member_id", nullable = false)
    @OrderBy("id ASC")
    val addresses: MutableList<MemberAddressJpaEntity> = mutableListOf(),
) {
    fun toDomain(): Member {
        return Member(
            id = id,
            email = Email(email),
            passwordHash = passwordHash,
            name = name,
            nickname = nickname,
            phoneNumber = phoneNumber,
            role = role,
            registeredAt = registeredAt,
            lastLoginAt = lastLoginAt,
            addresses = addresses.map(MemberAddressJpaEntity::toDomain).toMutableList(),
        )
    }

    companion object {
        fun from(member: Member): MemberJpaEntity {
            return MemberJpaEntity(
                id = member.id,
                email = member.email.address,
                passwordHash = member.passwordHash,
                name = member.name,
                nickname = member.nickname,
                phoneNumber = member.phoneNumber,
                role = member.role,
                registeredAt = member.registeredAt,
                lastLoginAt = member.lastLoginAt,
                addresses = member.addresses.map(MemberAddressJpaEntity::from).toMutableList(),
            )
        }
    }

    fun updateFrom(member: Member) {
        addresses.removeIf { existing -> member.addresses.none { it.id == existing.id } }

        val existingAddresses = addresses.associateBy { it.id }
        member.addresses.forEach { address ->
            val existing = existingAddresses[address.id]
            if (existing != null) {
                existing.updateFrom(address)
            } else {
                addresses.add(MemberAddressJpaEntity.from(address))
            }
        }
    }
}
