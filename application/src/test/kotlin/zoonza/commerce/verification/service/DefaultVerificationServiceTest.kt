package zoonza.commerce.verification.service

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.*
import zoonza.commerce.common.Email
import zoonza.commerce.exception.BusinessException
import zoonza.commerce.exception.ErrorCode
import zoonza.commerce.verification.EmailVerification
import zoonza.commerce.verification.VerificationPurpose
import zoonza.commerce.verification.port.out.VerificationCodeSender
import zoonza.commerce.verification.port.out.VerificationRepository
import java.time.LocalDateTime
import kotlin.test.Test

class DefaultVerificationServiceTest {
    private val verificationRepository = mockk<VerificationRepository>()
    private val verificationCodeSender = mockk<VerificationCodeSender>()
    private val verificationService =
        DefaultVerificationService(
            verificationRepository = verificationRepository,
            verificationCodeSender = verificationCodeSender,
        )

    companion object {
        private val VERIFICATION_CODE_PATTERN = Regex("""\d{3} \d{3}""")
    }

    @Test
    fun `신규 인증 요청이면 인증 정보를 저장하고 메일을 발송한다`() {
        val email = Email("member@example.com")
        val purpose = VerificationPurpose.SIGNUP
        val verificationSlot = slot<EmailVerification>()
        val codeSlot = slot<String>()

        every { verificationRepository.findByEmailAndPurpose(email, purpose) } returns null
        every { verificationRepository.save(capture(verificationSlot)) } answers { firstArg() }
        every {
            verificationCodeSender.sendVerificationCode(email, purpose, capture(codeSlot))
        } just Runs

        verificationService.issueEmailVerification(email, purpose)

        verify(exactly = 1) { verificationRepository.findByEmailAndPurpose(email, purpose) }
        verify(exactly = 1) { verificationRepository.save(any()) }
        verify(exactly = 1) {
            verificationCodeSender.sendVerificationCode(email, purpose, any())
        }

        VERIFICATION_CODE_PATTERN.matches(codeSlot.captured).shouldBeTrue()
        verificationSlot.captured.email shouldBe email
        verificationSlot.captured.purpose shouldBe purpose
        verificationSlot.captured.code shouldBe codeSlot.captured
    }

    @Test
    fun `기존 인증 요청이 있으면 인증 코드를 갱신한다`() {
        val email = Email("member@example.com")
        val purpose = VerificationPurpose.SIGNUP
        val previousIssuedAt = LocalDateTime.of(2026, 3, 1, 10, 0)
        val previousExpiresAt = previousIssuedAt.plusMinutes(5)
        val verification =
            EmailVerification.issue(
                email = email,
                purpose = purpose,
                code = "111 111",
                issuedAt = previousIssuedAt,
                expiresAt = previousExpiresAt,
            )

        every { verificationRepository.findByEmailAndPurpose(email, purpose) } returns verification
        every { verificationRepository.save(verification) } returns verification
        every { verificationCodeSender.sendVerificationCode(email, purpose, any()) } just Runs

        verificationService.issueEmailVerification(email, purpose)

        verify(exactly = 1) { verificationRepository.findByEmailAndPurpose(email, purpose) }
        verify(exactly = 1) { verificationRepository.save(verification) }
        verify(exactly = 1) {
            verificationCodeSender.sendVerificationCode(email, purpose, verification.code)
        }

        VERIFICATION_CODE_PATTERN.matches(verification.code).shouldBeTrue()
        verification.issuedAt.isAfter(previousIssuedAt).shouldBeTrue()
        verification.expiresAt.isAfter(previousExpiresAt).shouldBeTrue()
        verification.expiresAt shouldBe verification.issuedAt.plusMinutes(5)
        verification.verifiedAt shouldBe null
    }

    @Test
    fun `인증 코드가 일치하면 인증 완료로 저장한다`() {
        val email = Email("member@example.com")
        val purpose = VerificationPurpose.SIGNUP
        val verification =
            EmailVerification.issue(
                email = email,
                purpose = purpose,
                code = "123 456",
                issuedAt = LocalDateTime.now().minusMinutes(1),
                expiresAt = LocalDateTime.now().plusMinutes(4),
            )

        every { verificationRepository.findByEmailAndPurpose(email, purpose) } returns verification
        every { verificationRepository.save(verification) } returns verification

        verificationService.verifyEmailVerification(email, purpose, "123 456")

        verify(exactly = 1) { verificationRepository.findByEmailAndPurpose(email, purpose) }
        verify(exactly = 1) { verificationRepository.save(verification) }
        verification.verifiedAt.shouldNotBeNull()
    }

    @Test
    fun `인증 요청이 없으면 예외를 던진다`() {
        val email = Email("member@example.com")
        val purpose = VerificationPurpose.SIGNUP

        every { verificationRepository.findByEmailAndPurpose(email, purpose) } returns null

        val exception =
            io.kotest.assertions.throwables.shouldThrow<BusinessException> {
                verificationService.verifyEmailVerification(email, purpose, "123 456")
            }

        exception.errorCode shouldBe ErrorCode.EMAIL_VERIFICATION_NOT_FOUND
        verify(exactly = 1) { verificationRepository.findByEmailAndPurpose(email, purpose) }
        verify(exactly = 0) { verificationRepository.save(any()) }
    }

    @Test
    fun `인증 코드가 다르면 예외를 던진다`() {
        val email = Email("member@example.com")
        val purpose = VerificationPurpose.SIGNUP
        val verification =
            EmailVerification.issue(
                email = email,
                purpose = purpose,
                code = "123 456",
                issuedAt = LocalDateTime.now().minusMinutes(1),
                expiresAt = LocalDateTime.now().plusMinutes(4),
            )

        every { verificationRepository.findByEmailAndPurpose(email, purpose) } returns verification

        val exception =
            io.kotest.assertions.throwables.shouldThrow<BusinessException> {
                verificationService.verifyEmailVerification(email, purpose, "654 321")
            }

        exception.errorCode shouldBe ErrorCode.INVALID_VERIFICATION_CODE
        verify(exactly = 1) { verificationRepository.findByEmailAndPurpose(email, purpose) }
        verify(exactly = 0) { verificationRepository.save(any()) }
        verification.verifiedAt shouldBe null
    }

    @Test
    fun `인증 코드가 만료되면 예외를 던진다`() {
        val email = Email("member@example.com")
        val purpose = VerificationPurpose.SIGNUP
        val verification =
            EmailVerification.issue(
                email = email,
                purpose = purpose,
                code = "123 456",
                issuedAt = LocalDateTime.now().minusMinutes(10),
                expiresAt = LocalDateTime.now().minusMinutes(5),
            )

        every { verificationRepository.findByEmailAndPurpose(email, purpose) } returns verification

        val exception =
            io.kotest.assertions.throwables.shouldThrow<BusinessException> {
                verificationService.verifyEmailVerification(email, purpose, "123 456")
            }

        exception.errorCode shouldBe ErrorCode.EXPIRED_VERIFICATION_CODE
        verify(exactly = 1) { verificationRepository.findByEmailAndPurpose(email, purpose) }
        verify(exactly = 0) { verificationRepository.save(any()) }
        verification.verifiedAt shouldBe null
    }

    @Test
    fun `인증 완료된 이메일이면 가입 검증을 통과한다`() {
        val email = Email("member@example.com")
        val purpose = VerificationPurpose.SIGNUP
        val verification =
            EmailVerification.issue(
                email = email,
                purpose = purpose,
                code = "123 456",
                issuedAt = LocalDateTime.now().minusMinutes(1),
                expiresAt = LocalDateTime.now().plusMinutes(4),
            )

        verification.verify("123 456", LocalDateTime.now())
        every { verificationRepository.findByEmailAndPurpose(email, purpose) } returns verification

        verificationService.assertVerifiedEmail(email, purpose)

        verify(exactly = 1) { verificationRepository.findByEmailAndPurpose(email, purpose) }
        verify(exactly = 0) { verificationRepository.save(any()) }
    }

    @Test
    fun `인증 완료되지 않은 이메일이면 가입 검증에서 예외를 던진다`() {
        val email = Email("member@example.com")
        val purpose = VerificationPurpose.SIGNUP
        val verification =
            EmailVerification.issue(
                email = email,
                purpose = purpose,
                code = "123 456",
                issuedAt = LocalDateTime.now().minusMinutes(1),
                expiresAt = LocalDateTime.now().plusMinutes(4),
            )

        every { verificationRepository.findByEmailAndPurpose(email, purpose) } returns verification

        val exception =
            io.kotest.assertions.throwables.shouldThrow<BusinessException> {
                verificationService.assertVerifiedEmail(email, purpose)
            }

        exception.errorCode shouldBe ErrorCode.EMAIL_NOT_VERIFIED
        verify(exactly = 1) { verificationRepository.findByEmailAndPurpose(email, purpose) }
        verify(exactly = 0) { verificationRepository.save(any()) }
    }
}
