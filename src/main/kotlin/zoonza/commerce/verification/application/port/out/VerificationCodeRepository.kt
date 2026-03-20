package zoonza.commerce.verification.application.port.out

import zoonza.commerce.shared.Email
import zoonza.commerce.verification.domain.VerificationCode
import zoonza.commerce.verification.domain.VerificationPurpose


interface VerificationCodeRepository {
    fun findByEmailAndPurpose(
        email: Email,
        purpose: VerificationPurpose,
    ): VerificationCode?

    fun save(verification: VerificationCode): VerificationCode
}
