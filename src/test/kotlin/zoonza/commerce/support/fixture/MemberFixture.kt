package zoonza.commerce.support.fixture

import zoonza.commerce.member.adapter.out.persistence.MemberJpaEntity
import zoonza.commerce.member.domain.Member
import zoonza.commerce.member.domain.Role
import zoonza.commerce.shared.Email
import java.time.LocalDateTime

object MemberFixture {
    private val defaultRegisteredAt: LocalDateTime = LocalDateTime.of(2026, 3, 21, 8, 0)

    fun create(
        email: String = "member@example.com",
        passwordHash: String = "encoded-password",
        name: String = "회원",
        nickname: String = "nickname",
        phoneNumber: String = "01012345678",
        role: Role = Role.CUSTOMER,
        registeredAt: LocalDateTime = defaultRegisteredAt,
        lastLoginAt: LocalDateTime? = null,
    ): Member {
        return Member.create(
            email = Email(email),
            passwordHash = passwordHash,
            name = name,
            nickname = nickname,
            phoneNumber = phoneNumber,
            role = role,
            registeredAt = registeredAt,
            lastLoginAt = lastLoginAt,
        )
    }

    fun createIndexed(
        index: Int,
        emailPrefix: String = "member",
        namePrefix: String = "회원",
        nicknamePrefix: String = "nickname",
        phoneNumberPrefix: String = "0100000000",
        passwordHash: String = "encoded-password",
        role: Role = Role.CUSTOMER,
        registeredAt: LocalDateTime = defaultRegisteredAt,
    ): Member {
        return create(
            email = "$emailPrefix$index@example.com",
            passwordHash = passwordHash,
            name = "$namePrefix$index",
            nickname = "$nicknamePrefix$index",
            phoneNumber = "$phoneNumberPrefix$index",
            role = role,
            registeredAt = registeredAt,
        )
    }

    fun createJpa(
        email: String = "member@example.com",
        passwordHash: String = "encoded-password",
        name: String = "회원",
        nickname: String = "nickname",
        phoneNumber: String = "01012345678",
        role: Role = Role.CUSTOMER,
        registeredAt: LocalDateTime = defaultRegisteredAt,
        lastLoginAt: LocalDateTime? = null,
    ): MemberJpaEntity {
        return MemberJpaEntity.from(
            create(
                email = email,
                passwordHash = passwordHash,
                name = name,
                nickname = nickname,
                phoneNumber = phoneNumber,
                role = role,
                registeredAt = registeredAt,
                lastLoginAt = lastLoginAt,
            ),
        )
    }

    fun createIndexedJpa(
        index: Int,
        emailPrefix: String = "member",
        namePrefix: String = "회원",
        nicknamePrefix: String = "nickname",
        phoneNumberPrefix: String = "0100000000",
        passwordHash: String = "encoded-password",
        role: Role = Role.CUSTOMER,
        registeredAt: LocalDateTime = defaultRegisteredAt,
    ): MemberJpaEntity {
        return MemberJpaEntity.from(
            createIndexed(
                index = index,
                emailPrefix = emailPrefix,
                namePrefix = namePrefix,
                nicknamePrefix = nicknamePrefix,
                phoneNumberPrefix = phoneNumberPrefix,
                passwordHash = passwordHash,
                role = role,
                registeredAt = registeredAt,
            ),
        )
    }
}
