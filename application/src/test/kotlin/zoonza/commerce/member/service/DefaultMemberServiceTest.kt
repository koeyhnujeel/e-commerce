package zoonza.commerce.member.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import zoonza.commerce.common.Email
import zoonza.commerce.exception.BusinessException
import zoonza.commerce.exception.ErrorCode
import zoonza.commerce.member.Member
import zoonza.commerce.member.dto.SignUpCommand
import zoonza.commerce.member.port.out.MemberRepository
import zoonza.commerce.member.port.out.NicknameGenerator
import zoonza.commerce.member.port.out.PasswordHasher
import zoonza.commerce.verification.VerificationPurpose
import zoonza.commerce.verification.port.`in`.VerificationService
import kotlin.test.Test

class DefaultMemberServiceTest {
    private val memberRepository = mockk<MemberRepository>()
    private val verificationService = mockk<VerificationService>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val nicknameGenerator = mockk<NicknameGenerator>()
    private val memberService =
        DefaultMemberService(
            memberRepository = memberRepository,
            verificationService = verificationService,
            passwordHasher = passwordHasher,
            nicknameGenerator = nicknameGenerator,
        )

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

    @Test
    fun `회원가입 이메일 인증 코드 검증을 verification 서비스에 위임한다`() {
        val email = "member@example.com"
        val emailAddress = Email(email)
        val code = "123 456"

        every {
            verificationService.verifyEmailVerification(
                email = emailAddress,
                purpose = VerificationPurpose.SIGNUP,
                code = code,
            )
        } just Runs

        memberService.verifySignupEmailCode(email, code)

        verify(exactly = 1) {
            verificationService.verifyEmailVerification(
                email = emailAddress,
                purpose = VerificationPurpose.SIGNUP,
                code = code,
            )
        }
    }

    @Test
    fun `회원가입에 성공하면 암호화된 비밀번호와 랜덤 닉네임으로 저장한다`() {
        val email = "member@example.com"
        val password = "password123!"
        val name = "홍길동"
        val phoneNumber = "01012345678"
        val emailAddress = Email(email)
        val nickname = "반짝이는판다1"
        val memberSlot = slot<Member>()
        val command =
            SignUpCommand(
                email = email,
                password = password,
                name = name,
                phoneNumber = phoneNumber,
            )

        every { memberRepository.existsByEmail(emailAddress) } returns false
        every { memberRepository.existsByPhoneNumber(phoneNumber) } returns false
        every {
            verificationService.assertVerifiedEmail(emailAddress, VerificationPurpose.SIGNUP)
        } just Runs
        every { passwordHasher.hash(password) } returns "encoded-password"
        every { nicknameGenerator.generate() } returns nickname
        every { memberRepository.existsByNickname(nickname) } returns false
        every { memberRepository.save(capture(memberSlot)) } answers {
            firstArg<Member>().let {
                Member.create(
                    email = it.email,
                    passwordHash = it.passwordHash,
                    name = it.name,
                    nickname = it.nickname,
                    phoneNumber = it.phoneNumber,
                    registeredAt = it.registeredAt,
                )
            }
        }

        val memberId = memberService.signUp(command)

        memberId shouldBe 0L
        verify(exactly = 1) { memberRepository.existsByEmail(emailAddress) }
        verify(exactly = 1) { memberRepository.existsByPhoneNumber(phoneNumber) }
        verify(exactly = 1) {
            verificationService.assertVerifiedEmail(emailAddress, VerificationPurpose.SIGNUP)
        }
        verify(exactly = 1) { passwordHasher.hash(password) }
        verify(exactly = 1) { nicknameGenerator.generate() }
        verify(exactly = 1) { memberRepository.existsByNickname(nickname) }
        verify(exactly = 1) { memberRepository.save(any()) }

        memberSlot.captured.email shouldBe emailAddress
        memberSlot.captured.passwordHash shouldBe "encoded-password"
        memberSlot.captured.name shouldBe name
        memberSlot.captured.nickname shouldBe nickname
        memberSlot.captured.phoneNumber shouldBe phoneNumber
    }

    @Test
    fun `회원가입 시 이미 가입된 이메일이면 예외를 던진다`() {
        val email = "member@example.com"
        val emailAddress = Email(email)

        every { memberRepository.existsByEmail(emailAddress) } returns true

        val exception =
            shouldThrow<BusinessException> {
                memberService.signUp(
                    SignUpCommand(
                        email = email,
                        password = "password123!",
                        name = "홍길동",
                        phoneNumber = "01012345678",
                    ),
                )
            }

        exception.errorCode shouldBe ErrorCode.DUPLICATE_EMAIL
        verify(exactly = 1) { memberRepository.existsByEmail(emailAddress) }
        verify(exactly = 0) { memberRepository.existsByPhoneNumber(any()) }
        verify(exactly = 0) { verificationService.assertVerifiedEmail(any(), any()) }
    }

    @Test
    fun `회원가입 시 휴대폰 번호가 중복되면 예외를 던진다`() {
        val email = "member@example.com"
        val emailAddress = Email(email)
        val phoneNumber = "01012345678"

        every { memberRepository.existsByEmail(emailAddress) } returns false
        every { memberRepository.existsByPhoneNumber(phoneNumber) } returns true

        val exception =
            shouldThrow<BusinessException> {
                memberService.signUp(
                    SignUpCommand(
                        email = email,
                        password = "password123!",
                        name = "홍길동",
                        phoneNumber = phoneNumber,
                    ),
                )
            }

        exception.errorCode shouldBe ErrorCode.DUPLICATE_PHONE_NUMBER
        verify(exactly = 1) { memberRepository.existsByEmail(emailAddress) }
        verify(exactly = 1) { memberRepository.existsByPhoneNumber(phoneNumber) }
        verify(exactly = 0) { verificationService.assertVerifiedEmail(any(), any()) }
    }

    @Test
    fun `회원가입 시 이메일 인증이 완료되지 않았으면 예외를 던진다`() {
        val email = "member@example.com"
        val emailAddress = Email(email)
        val phoneNumber = "01012345678"

        every { memberRepository.existsByEmail(emailAddress) } returns false
        every { memberRepository.existsByPhoneNumber(phoneNumber) } returns false
        every {
            verificationService.assertVerifiedEmail(emailAddress, VerificationPurpose.SIGNUP)
        } throws BusinessException(ErrorCode.EMAIL_NOT_VERIFIED)

        val exception =
            shouldThrow<BusinessException> {
                memberService.signUp(
                    SignUpCommand(
                        email = email,
                        password = "password123!",
                        name = "홍길동",
                        phoneNumber = phoneNumber,
                    ),
                )
            }

        exception.errorCode shouldBe ErrorCode.EMAIL_NOT_VERIFIED
        verify(exactly = 0) { passwordHasher.hash(any()) }
        verify(exactly = 0) { nicknameGenerator.generate() }
        verify(exactly = 0) { memberRepository.save(any()) }
    }

    @Test
    fun `회원가입 시 생성된 닉네임이 중복되면 다시 생성한다`() {
        val email = "member@example.com"
        val password = "password123!"
        val phoneNumber = "01012345678"
        val emailAddress = Email(email)
        val duplicateNickname = "반짝이는판다1"
        val uniqueNickname = "빛나는여우2"

        every { memberRepository.existsByEmail(emailAddress) } returns false
        every { memberRepository.existsByPhoneNumber(phoneNumber) } returns false
        every {
            verificationService.assertVerifiedEmail(emailAddress, VerificationPurpose.SIGNUP)
        } just Runs
        every { passwordHasher.hash(password) } returns "encoded-password"
        every { nicknameGenerator.generate() } returnsMany listOf(duplicateNickname, uniqueNickname)
        every { memberRepository.existsByNickname(duplicateNickname) } returns true
        every { memberRepository.existsByNickname(uniqueNickname) } returns false
        every { memberRepository.save(any()) } answers { firstArg() }

        val memberId =
            memberService.signUp(
                SignUpCommand(
                    email = email,
                    password = password,
                    name = "홍길동",
                    phoneNumber = phoneNumber,
                ),
            )

        memberId shouldBe 0L
        verify(exactly = 2) { nicknameGenerator.generate() }
        verify(exactly = 1) { memberRepository.existsByNickname(duplicateNickname) }
        verify(exactly = 1) { memberRepository.existsByNickname(uniqueNickname) }
    }
}
