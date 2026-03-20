package zoonza.commerce.verification.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.verification.domain.VerificationCode
import zoonza.commerce.verification.domain.VerificationPurpose

interface VerificationCodeJpaRepository : JpaRepository<VerificationCode, Long> {
    fun findByEmailAddressAndPurpose(
        email: String,
        purpose: VerificationPurpose,
    ): VerificationCode?
}
