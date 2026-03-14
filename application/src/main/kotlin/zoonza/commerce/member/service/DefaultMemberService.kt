package zoonza.commerce.member.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.common.Email
import zoonza.commerce.exception.BusinessException
import zoonza.commerce.exception.ErrorCode
import zoonza.commerce.member.Member
import zoonza.commerce.member.dto.SignupCommand
import zoonza.commerce.member.port.`in`.MemberService
import zoonza.commerce.member.port.out.MemberRepository
import zoonza.commerce.member.port.out.NicknameGenerator
import zoonza.commerce.member.port.out.PasswordHasher
import zoonza.commerce.verification.VerificationPurpose
import zoonza.commerce.verification.port.`in`.VerificationService
import java.time.LocalDateTime

@Service
class DefaultMemberService(
    private val memberRepository: MemberRepository,
    private val verificationService: VerificationService,
    private val passwordHasher: PasswordHasher,
    private val nicknameGenerator: NicknameGenerator,
) : MemberService {
    @Transactional
    override fun sendSignupEmailVerificationCode(email: String) {
        val emailAddress = Email(email)

        if (memberRepository.existsByEmail(emailAddress)) {
            throw BusinessException(ErrorCode.DUPLICATE_EMAIL)
        }

        verificationService.issueEmailVerification(emailAddress, VerificationPurpose.SIGNUP)
    }

    @Transactional
    override fun verifySignupEmailCode(email: String, code: String) {
        verificationService.verifyEmailVerification(
            email = Email(email),
            purpose = VerificationPurpose.SIGNUP,
            code = code,
        )
    }

    @Transactional
    override fun signup(command: SignupCommand): Long {
        val emailAddress = Email(command.email)

        if (memberRepository.existsByEmail(emailAddress)) {
            throw BusinessException(ErrorCode.DUPLICATE_EMAIL)
        }

        if (memberRepository.existsByPhoneNumber(command.phoneNumber)) {
            throw BusinessException(ErrorCode.DUPLICATE_PHONE_NUMBER)
        }

        verificationService.assertVerifiedEmail(emailAddress, VerificationPurpose.SIGNUP)

        val member =
            Member.create(
                email = emailAddress,
                passwordHash = passwordHasher.hash(command.password),
                name = command.name,
                nickname = generateUniqueNickname(),
                phoneNumber = command.phoneNumber,
                registeredAt = LocalDateTime.now(),
            )

        return memberRepository.save(member).id
    }

    private fun generateUniqueNickname(): String {
        repeat(MAX_NICKNAME_GENERATION_ATTEMPTS) {
            val nickname = nicknameGenerator.generate()
            if (!memberRepository.existsByNickname(nickname)) {
                return nickname
            }
        }

        throw IllegalStateException("중복되지 않는 닉네임을 생성하지 못했습니다.")
    }

    companion object {
        private const val MAX_NICKNAME_GENERATION_ATTEMPTS = 100
    }
}
