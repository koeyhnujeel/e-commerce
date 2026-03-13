package zoonza.commerce.member.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import zoonza.commerce.common.Email
import zoonza.commerce.exception.BusinessException
import zoonza.commerce.exception.ErrorCode
import zoonza.commerce.member.port.out.MemberRepository
import zoonza.commerce.verification.VerificationPurpose
import zoonza.commerce.verification.port.`in`.VerificationService
import kotlin.test.Test

class DefaultMemberServiceTest {
    private val memberRepository = mockk<MemberRepository>()
    private val verificationService = mockk<VerificationService>()
    private val memberService = DefaultMemberService(memberRepository, verificationService)

    @Test
    fun `회원가입 가능한 이메일이면 verification 서비스에 코드 발급을 위임한다`() {
        val email = "member@example.com"
        val emailAddress = Email(email)

        every { memberRepository.existsByEmail(emailAddress) } returns false
        every {
            verificationService.issueEmailVerification(emailAddress, VerificationPurpose.SIGNUP)
        } just Runs

        memberService.sendSignupEmailVerificationCode(email)

        verify(exactly = 1) { memberRepository.existsByEmail(emailAddress) }
        verify(exactly = 1) {
            verificationService.issueEmailVerification(emailAddress, VerificationPurpose.SIGNUP)
        }
    }

    @Test
    fun `이미 가입된 이메일이면 예외를 던진다`() {
        val email = "member@example.com"
        val emailAddress = Email(email)

        every { memberRepository.existsByEmail(emailAddress) } returns true

        val exception = shouldThrow<BusinessException> {
            memberService.sendSignupEmailVerificationCode(email)
        }

        exception.errorCode shouldBe ErrorCode.DUPLICATE_EMAIL
        exception.message shouldBe ErrorCode.DUPLICATE_EMAIL.message

        verify(exactly = 1) { memberRepository.existsByEmail(emailAddress) }
        verify(exactly = 0) {
            verificationService.issueEmailVerification(any(), any())
        }
    }
}
