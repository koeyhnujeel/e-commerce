package zoonza.commerce.verification

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import zoonza.commerce.common.Email
import zoonza.commerce.exception.BusinessException
import zoonza.commerce.exception.ErrorCode
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
class EmailVerification private constructor(
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
            throw BusinessException(ErrorCode.EMAIL_NOT_VERIFIED)
        }
    }

    private fun validateCode(code: String) {
        if (this.code != code) {
            throw BusinessException(ErrorCode.INVALID_VERIFICATION_CODE)
        }
    }

    private fun validateNotExpired(verifiedAt: LocalDateTime) {
        if (verifiedAt.isAfter(expiresAt)) {
            throw BusinessException(ErrorCode.EXPIRED_VERIFICATION_CODE)
        }
    }

    companion object {
        fun issue(
            email: Email,
            purpose: VerificationPurpose,
            code: String,
            issuedAt: LocalDateTime,
            expiresAt: LocalDateTime,
        ): EmailVerification {
            return EmailVerification(
                email = email,
                purpose = purpose,
                code = code,
                issuedAt = issuedAt,
                expiresAt = expiresAt,
            )
        }
    }
}
