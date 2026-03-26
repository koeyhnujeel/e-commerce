package zoonza.commerce.verification.application.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.Email
import zoonza.commerce.verification.VerificationApi
import zoonza.commerce.verification.VerificationCodeCreated
import zoonza.commerce.verification.VerificationErrorCode
import zoonza.commerce.verification.application.port.out.VerificationCodeRepository
import zoonza.commerce.verification.domain.VerificationCode
import zoonza.commerce.verification.domain.VerificationPurpose
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

@Service
class DefaultVerificationService(
    private val verificationCodeRepository: VerificationCodeRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : VerificationApi {
    companion object {
        private const val EXPIRATION_MINUTES = 5L
    }

    @Transactional
    override fun createSignupEmailVerificationCode(email: Email) {
        val purpose = VerificationPurpose.SIGNUP
        val issuedAt = LocalDateTime.now()
        val expiresAt = issuedAt.plusMinutes(EXPIRATION_MINUTES)
        val code = generateVerificationCode()

        val verificationCode = verificationCodeRepository.findByEmailAndPurpose(email, purpose)
            ?.also { it.renew(code, issuedAt, expiresAt) }
            ?: VerificationCode.create(
                email = email,
                purpose = VerificationPurpose.SIGNUP,
                code = code,
                issuedAt = issuedAt,
                expiresAt = expiresAt,
            )

        verificationCodeRepository.save(verificationCode)

        eventPublisher.publishEvent(VerificationCodeCreated(email, code))
    }

    @Transactional
    override fun verifySignupEmailVerificationCode(email: Email, code: String) {
        val purpose = VerificationPurpose.SIGNUP

        val verification = verificationCodeRepository.findByEmailAndPurpose(email, purpose)
            ?: throw BusinessException(VerificationErrorCode.EMAIL_VERIFICATION_NOT_FOUND)

        verification.verify(code, LocalDateTime.now())

        verificationCodeRepository.save(verification)
    }

    @Transactional(readOnly = true)
    override fun assertVerifiedSignupEmail(email: Email) {
        val purpose = VerificationPurpose.SIGNUP

        val verification = verificationCodeRepository.findByEmailAndPurpose(email, purpose)
            ?: throw BusinessException(VerificationErrorCode.EMAIL_NOT_VERIFIED)

        verification.assertVerified()
    }

    private fun generateVerificationCode(): String {
        val code = SecureRandom().nextInt(999_999) + 1
        return String.format(Locale.ROOT, "%03d %03d", code / 1_000, code % 1_000)
    }
}
