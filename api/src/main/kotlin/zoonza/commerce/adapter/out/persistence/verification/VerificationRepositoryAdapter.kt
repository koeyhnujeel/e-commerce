package zoonza.commerce.adapter.out.persistence.verification

import org.springframework.stereotype.Repository
import zoonza.commerce.common.Email
import zoonza.commerce.verification.EmailVerification
import zoonza.commerce.verification.VerificationPurpose
import zoonza.commerce.verification.port.out.VerificationRepository

@Repository
class VerificationRepositoryAdapter(
    private val emailVerificationJpaRepository: EmailVerificationJpaRepository,
) : VerificationRepository {
    override fun findByEmailAndPurpose(
        email: Email,
        purpose: VerificationPurpose,
    ): EmailVerification? {
        return emailVerificationJpaRepository.findByEmailAddressAndPurpose(
            email.address,
            purpose,
        )
    }

    override fun save(verification: EmailVerification): EmailVerification {
        return emailVerificationJpaRepository.save(verification)
    }
}
