package zoonza.commerce.member.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test
import zoonza.commerce.member.AuthenticatedMember
import zoonza.commerce.member.application.dto.SignupCommand
import zoonza.commerce.member.application.port.out.MemberRepository
import zoonza.commerce.member.application.port.out.NicknameGenerator
import zoonza.commerce.member.domain.Member
import zoonza.commerce.member.domain.PasswordEncoder
import zoonza.commerce.shared.AuthException
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.Email
import zoonza.commerce.shared.ErrorCode
import zoonza.commerce.verification.VerificationApi
import java.time.LocalDateTime

class DefaultMemberServiceTest {
    private val memberRepository = mockk<MemberRepository>()
    private val verificationApi = mockk<VerificationApi>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val nicknameGenerator = mockk<NicknameGenerator>()
    private val memberService =
        DefaultMemberService(
            memberRepository = memberRepository,
            verificationApi = verificationApi,
            passwordEncoder = passwordEncoder,
            nicknameGenerator = nicknameGenerator,
        )

    @Test
    fun `회원가입 이메일 인증 코드 발송 전에 중복 이메일을 검사한다`() {
        val email = Email("member@example.com")
        every { memberRepository.existsByEmail(email) } returns false
        every { verificationApi.createSignupEmailVerificationCode(email) } just Runs

        memberService.sendSignupEmailVerificationCode(email.address)

        verify(exactly = 1) { verificationApi.createSignupEmailVerificationCode(email) }
    }

    @Test
    fun `중복된 이메일이면 회원가입 이메일 인증 코드 발송에 실패한다`() {
        val email = Email("member@example.com")
        every { memberRepository.existsByEmail(email) } returns true

        val exception =
            shouldThrow<BusinessException> {
                memberService.sendSignupEmailVerificationCode(email.address)
            }

        exception.errorCode shouldBe ErrorCode.DUPLICATE_EMAIL
        verify(exactly = 0) { verificationApi.createSignupEmailVerificationCode(any()) }
    }

    @Test
    fun `회원가입에 성공하면 저장된 회원 식별자를 반환한다`() {
        val email = Email("member@example.com")
        val savedMember = slot<Member>()
        val persistedMember = mockk<Member>()

        every { memberRepository.existsByEmail(email) } returns false
        every { memberRepository.existsByPhoneNumber("01012345678") } returns false
        every { verificationApi.assertVerifiedSignupEmail(email) } just Runs
        every { passwordEncoder.encode("Password123!") } returns "encoded-password"
        every { nicknameGenerator.generate() } returnsMany listOf("duplicate", "available")
        every { memberRepository.existsByNickname("duplicate") } returns true
        every { memberRepository.existsByNickname("available") } returns false
        every { memberRepository.save(capture(savedMember)) } returns persistedMember
        every { persistedMember.id } returns 1L

        val result =
            memberService.signup(
                SignupCommand(
                    email = email.address,
                    password = "Password123!",
                    name = "주문자",
                    phoneNumber = "01012345678",
                ),
            )

        result shouldBe 1L
        savedMember.captured.email shouldBe email
        savedMember.captured.passwordHash shouldBe "encoded-password"
        savedMember.captured.nickname shouldBe "available"
        savedMember.captured.registeredAt.shouldNotBeNull()
    }

    @Test
    fun `이메일과 비밀번호가 맞으면 인증된 회원 정보를 반환한다`() {
        val email = Email("member@example.com")
        val member =
            Member.create(
                email = email,
                passwordHash = "encoded-password",
                name = "주문자",
                nickname = "nickname",
                phoneNumber = "01012345678",
                registeredAt = LocalDateTime.of(2026, 3, 21, 10, 0),
            )

        every { memberRepository.findByEmail(email) } returns member
        every { passwordEncoder.matches("Password123!", "encoded-password") } returns true

        val authenticatedMember = memberService.authenticate(email, "Password123!")

        authenticatedMember shouldBe AuthenticatedMember(member.id, member.email, member.role.name)
        member.lastLoginAt.shouldNotBeNull()
    }

    @Test
    fun `회원이 없으면 인증에 실패한다`() {
        val email = Email("member@example.com")
        every { memberRepository.findByEmail(email) } returns null

        val exception =
            shouldThrow<AuthException> {
                memberService.authenticate(email, "Password123!")
            }

        exception.errorCode shouldBe ErrorCode.INVALID_CREDENTIALS
    }

    @Test
    fun `회원 식별자로 인증된 회원 정보를 조회한다`() {
        val member =
            Member.create(
                email = Email("member@example.com"),
                passwordHash = "encoded-password",
                name = "주문자",
                nickname = "nickname",
                phoneNumber = "01012345678",
                registeredAt = LocalDateTime.of(2026, 3, 21, 10, 0),
            )
        every { memberRepository.findById(1L) } returns member

        val found = memberService.findById(1L)

        found shouldBe AuthenticatedMember(member.id, member.email, member.role.name)
    }
}
