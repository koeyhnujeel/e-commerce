package zoonza.commerce.verification.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.common.Email
import zoonza.commerce.exception.BusinessException
import zoonza.commerce.exception.ErrorCode
import zoonza.commerce.verification.EmailVerification
import zoonza.commerce.verification.VerificationPurpose
import zoonza.commerce.verification.port.`in`.VerificationService
import zoonza.commerce.verification.port.out.VerificationCodeSender
import zoonza.commerce.verification.port.out.VerificationRepository
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.Locale

@Service
class DefaultVerificationService(
    private val verificationRepository: VerificationRepository,
    private val verificationCodeSender: VerificationCodeSender,
) : VerificationService {
    @Transactional
    override fun issueEmailVerification(
        email: Email,
        purpose: VerificationPurpose,
    ) {
        val issuedAt = LocalDateTime.now()
        val expiresAt = issuedAt.plusMinutes(EXPIRATION_MINUTES)
        val code = generateVerificationCode()

        val verification =
            verificationRepository.findByEmailAndPurpose(email, purpose)
                ?.also { it.renew(code, issuedAt, expiresAt) }
                ?: EmailVerification.issue(
                    email = email,
                    purpose = purpose,
                    code = code,
                    issuedAt = issuedAt,
                    expiresAt = expiresAt,
                )

        verificationRepository.save(verification)
        verificationCodeSender.sendVerificationCode(email, purpose, code)
    }

    @Transactional
    override fun verifyEmailVerification(
        email: Email,
        purpose: VerificationPurpose,
        code: String,
    ) {
        val verification =
            verificationRepository.findByEmailAndPurpose(email, purpose)
                ?: throw BusinessException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND)

        verification.verify(code, LocalDateTime.now())
        verificationRepository.save(verification)
    }

    private fun generateVerificationCode(): String {
        val code = SecureRandom().nextInt(999_999) + 1
        return String.format(Locale.ROOT, "%03d %03d", code / 1_000, code % 1_000)
    }

    companion object {
        private const val EXPIRATION_MINUTES = 5L
    }
}
