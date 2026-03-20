package zoonza.commerce.verification.adapter.out.persistence

import org.springframework.stereotype.Repository
import zoonza.commerce.shared.Email
import zoonza.commerce.verification.application.port.out.VerificationCodeRepository
import zoonza.commerce.verification.domain.VerificationCode
import zoonza.commerce.verification.domain.VerificationPurpose

@Repository
class VerificationCodeRepositoryAdapter(
    private val verificationCodeJpaRepository: VerificationCodeJpaRepository,
) : VerificationCodeRepository {
    override fun findByEmailAndPurpose(
        email: Email,
        purpose: VerificationPurpose,
    ): VerificationCode? {
        return verificationCodeJpaRepository.findByEmailAddressAndPurpose(email.address, purpose)
    }

    override fun save(verification: VerificationCode): VerificationCode {
        return verificationCodeJpaRepository.save(verification)
    }
}
