package zoonza.commerce.verification.port.out

import zoonza.commerce.common.Email
import zoonza.commerce.verification.EmailVerification
import zoonza.commerce.verification.VerificationPurpose

interface VerificationRepository {
    fun findByEmailAndPurpose(
        email: Email,
        purpose: VerificationPurpose,
    ): EmailVerification?

    fun save(verification: EmailVerification): EmailVerification
}
