package zoonza.commerce.verification.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.verification.domain.VerificationPurpose

interface VerificationCodeJpaRepository : JpaRepository<VerificationCodeJpaEntity, Long> {
    fun findByEmailAndPurpose(
        email: String,
        purpose: VerificationPurpose,
    ): VerificationCodeJpaEntity?
}
