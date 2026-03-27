package zoonza.commerce.support.fixture

import zoonza.commerce.shared.Email
import zoonza.commerce.verification.adapter.out.persistence.VerificationCodeJpaEntity
import zoonza.commerce.verification.domain.VerificationCode
import zoonza.commerce.verification.domain.VerificationPurpose
import java.time.LocalDateTime

object VerificationCodeFixture {
    private val defaultIssuedAt: LocalDateTime = LocalDateTime.of(2026, 3, 21, 8, 0)

    fun create(
        email: String = "member@example.com",
        purpose: VerificationPurpose = VerificationPurpose.SIGNUP,
        code: String = "123 456",
        issuedAt: LocalDateTime = defaultIssuedAt,
        expiresAt: LocalDateTime = issuedAt.plusMinutes(5),
    ): VerificationCode {
        return VerificationCode.create(
            email = Email(email),
            purpose = purpose,
            code = code,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
        )
    }

    fun createJpa(
        email: String = "member@example.com",
        purpose: VerificationPurpose = VerificationPurpose.SIGNUP,
        code: String = "123 456",
        issuedAt: LocalDateTime = defaultIssuedAt,
        expiresAt: LocalDateTime = issuedAt.plusMinutes(5),
    ): VerificationCodeJpaEntity {
        return VerificationCodeJpaEntity.from(
            create(
                email = email,
                purpose = purpose,
                code = code,
                issuedAt = issuedAt,
                expiresAt = expiresAt,
            ),
        )
    }
}
