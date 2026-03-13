package zoonza.commerce.verification.service

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.*
import zoonza.commerce.common.Email
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
}
