package zoonza.commerce.verification.domain

import jakarta.persistence.*
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.Email
import zoonza.commerce.verification.VerificationErrorCode
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
class VerificationCode private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Embedded
    val email: Email,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val purpose: VerificationPurpose,

    @Column(nullable = false)
    var code: String,

    @Column(nullable = false)
    var issuedAt: LocalDateTime,

    @Column(nullable = false)
    var expiresAt: LocalDateTime,

    @Column
    var verifiedAt: LocalDateTime? = null,
) {
    companion object {
        fun create(
            email: Email,
            purpose: VerificationPurpose,
            code: String,
            issuedAt: LocalDateTime,
            expiresAt: LocalDateTime,
        ): VerificationCode {
            return VerificationCode(
                email = email,
                purpose = purpose,
                code = code,
                issuedAt = issuedAt,
                expiresAt = expiresAt,
            )
        }
    }

    fun renew(
        code: String,
        issuedAt: LocalDateTime,
        expiresAt: LocalDateTime,
    ) {
        this.code = code
        this.issuedAt = issuedAt
        this.expiresAt = expiresAt
        this.verifiedAt = null
    }

    fun verify(
        code: String,
        verifiedAt: LocalDateTime,
    ) {
        validateCode(code)

        if (this.verifiedAt != null) {
            return
        }

        validateNotExpired(verifiedAt)

        this.verifiedAt = verifiedAt
    }

    fun assertVerified() {
        if (verifiedAt == null) {
            throw BusinessException(VerificationErrorCode.EMAIL_NOT_VERIFIED)
        }
    }

    private fun validateCode(code: String) {
        if (this.code != code) {
            throw BusinessException(VerificationErrorCode.INVALID_VERIFICATION_CODE)
        }
    }

    private fun validateNotExpired(verifiedAt: LocalDateTime) {
        if (verifiedAt.isAfter(expiresAt)) {
            throw BusinessException(VerificationErrorCode.EXPIRED_VERIFICATION_CODE)
        }
    }
}
