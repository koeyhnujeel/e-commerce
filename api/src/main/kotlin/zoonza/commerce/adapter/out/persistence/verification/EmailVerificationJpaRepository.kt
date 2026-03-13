package zoonza.commerce.adapter.out.persistence.verification

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.verification.EmailVerification
import zoonza.commerce.verification.VerificationPurpose

interface EmailVerificationJpaRepository : JpaRepository<EmailVerification, Long> {
    fun findByEmailAddressAndPurpose(
        email: String,
        purpose: VerificationPurpose,
    ): EmailVerification?
}
