package zoonza.commerce.verification.domain

import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.Email
import zoonza.commerce.verification.VerificationErrorCode
import java.time.LocalDateTime

class VerificationCode(
    val id: Long = 0,
    val email: Email,
    val purpose: VerificationPurpose,
    var code: String,
    var issuedAt: LocalDateTime,
    var expiresAt: LocalDateTime,
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
