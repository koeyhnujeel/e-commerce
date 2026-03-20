package zoonza.commerce.verification.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.Email
import zoonza.commerce.shared.ErrorCode
import zoonza.commerce.verification.VerificationCodeCreated
import zoonza.commerce.verification.application.port.out.VerificationCodeRepository
import zoonza.commerce.verification.domain.VerificationCode
import zoonza.commerce.verification.domain.VerificationPurpose
import java.time.LocalDateTime

class DefaultVerificationServiceTest {
    private val verificationCodeRepository = mockk<VerificationCodeRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val verificationService =
        DefaultVerificationService(
            verificationCodeRepository = verificationCodeRepository,
            eventPublisher = eventPublisher,
        )

    @Test
    fun `회원가입 이메일 인증 코드를 새로 만들고 이벤트를 발행한다`() {
        val email = Email("member@example.com")
        val savedVerification = slot<VerificationCode>()

        every { verificationCodeRepository.findByEmailAndPurpose(email, VerificationPurpose.SIGNUP) } returns null
        every { verificationCodeRepository.save(capture(savedVerification)) } answers { savedVerification.captured }
        verificationService.createSignupEmailVerificationCode(email)

        savedVerification.captured.email shouldBe email
        savedVerification.captured.purpose shouldBe VerificationPurpose.SIGNUP
        savedVerification.captured.code.shouldNotBeNull()
        verify(exactly = 1) {
            eventPublisher.publishEvent(
                withArg<VerificationCodeCreated> { event ->
                    event.email shouldBe email
                    event.code shouldBe savedVerification.captured.code
                },
            )
        }
    }

    @Test
    fun `기존 인증 코드가 있으면 갱신하고 다시 저장한다`() {
        val email = Email("member@example.com")
        val existingVerification =
            VerificationCode.create(
                email = email,
                purpose = VerificationPurpose.SIGNUP,
                code = "123 456",
                issuedAt = LocalDateTime.of(2026, 3, 21, 10, 0),
                expiresAt = LocalDateTime.of(2026, 3, 21, 10, 5),
            )
        existingVerification.verify("123 456", LocalDateTime.of(2026, 3, 21, 10, 1))
        every { verificationCodeRepository.findByEmailAndPurpose(email, VerificationPurpose.SIGNUP) } returns existingVerification
        every { verificationCodeRepository.save(existingVerification) } returns existingVerification
        verificationService.createSignupEmailVerificationCode(email)

        existingVerification.verifiedAt.shouldBeNull()
        existingVerification.expiresAt shouldBe existingVerification.issuedAt.plusMinutes(5)
        verify(exactly = 1) { verificationCodeRepository.save(existingVerification) }
        verify(exactly = 1) { eventPublisher.publishEvent(any<VerificationCodeCreated>()) }
    }

    @Test
    fun `인증 코드가 일치하면 검증 상태를 저장한다`() {
        val email = Email("member@example.com")
        val verificationCode =
            VerificationCode.create(
                email = email,
                purpose = VerificationPurpose.SIGNUP,
                code = "123 456",
                issuedAt = LocalDateTime.now().minusMinutes(1),
                expiresAt = LocalDateTime.now().plusMinutes(4),
            )

        every { verificationCodeRepository.findByEmailAndPurpose(email, VerificationPurpose.SIGNUP) } returns verificationCode
        every { verificationCodeRepository.save(verificationCode) } returns verificationCode

        verificationService.verifySignupEmailVerificationCode(email, "123 456")

        verificationCode.verifiedAt.shouldNotBeNull()
        verify(exactly = 1) { verificationCodeRepository.save(verificationCode) }
    }

    @Test
    fun `인증 요청이 없으면 검증에 실패한다`() {
        val email = Email("member@example.com")
        every { verificationCodeRepository.findByEmailAndPurpose(email, VerificationPurpose.SIGNUP) } returns null

        val exception =
            shouldThrow<BusinessException> {
                verificationService.verifySignupEmailVerificationCode(email, "123 456")
            }

        exception.errorCode shouldBe ErrorCode.EMAIL_VERIFICATION_NOT_FOUND
    }

    @Test
    fun `인증되지 않은 이메일이면 검증 완료 상태 확인에 실패한다`() {
        val email = Email("member@example.com")
        val verificationCode =
            VerificationCode.create(
                email = email,
                purpose = VerificationPurpose.SIGNUP,
                code = "123 456",
                issuedAt = LocalDateTime.now().minusMinutes(1),
                expiresAt = LocalDateTime.now().plusMinutes(4),
            )
        every { verificationCodeRepository.findByEmailAndPurpose(email, VerificationPurpose.SIGNUP) } returns verificationCode

        val exception =
            shouldThrow<BusinessException> {
                verificationService.assertVerifiedSignupEmail(email)
            }

        exception.errorCode shouldBe ErrorCode.EMAIL_NOT_VERIFIED
    }
}
