package zoonza.commerce.verification.adapter.out.persistence

import jakarta.persistence.*
import zoonza.commerce.shared.Email
import zoonza.commerce.verification.domain.VerificationCode
import zoonza.commerce.verification.domain.VerificationPurpose
import java.time.LocalDateTime

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_email_verification_email_purpose",
            columnNames = ["email", "purpose"],
        ),
    ],
)
class VerificationCodeJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "email", nullable = false)
    val email: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val purpose: VerificationPurpose = VerificationPurpose.SIGNUP,

    @Column(nullable = false)
    val code: String = "",

    @Column(nullable = false)
    val issuedAt: LocalDateTime = LocalDateTime.MIN,

    @Column(nullable = false)
    val expiresAt: LocalDateTime = LocalDateTime.MIN,

    @Column
    val verifiedAt: LocalDateTime? = null,
) {
    fun toDomain(): VerificationCode {
        return VerificationCode(
            id = id,
            email = Email(email),
            purpose = purpose,
            code = code,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            verifiedAt = verifiedAt,
        )
    }

    companion object {
        fun from(verificationCode: VerificationCode): VerificationCodeJpaEntity {
            return VerificationCodeJpaEntity(
                id = verificationCode.id,
                email = verificationCode.email.address,
                purpose = verificationCode.purpose,
                code = verificationCode.code,
                issuedAt = verificationCode.issuedAt,
                expiresAt = verificationCode.expiresAt,
                verifiedAt = verificationCode.verifiedAt,
            )
        }
    }
}
