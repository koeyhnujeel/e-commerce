package zoonza.commerce.member.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.member.AuthenticatedMember
import zoonza.commerce.member.MemberApi
import zoonza.commerce.member.application.dto.SignupCommand
import zoonza.commerce.member.application.port.`in`.MemberService
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

@Service
class DefaultMemberService(
    private val memberRepository: MemberRepository,
    private val verificationApi: VerificationApi,
    private val passwordEncoder: PasswordEncoder,
    private val nicknameGenerator: NicknameGenerator,
) : MemberService, MemberApi {
    companion object {
        private const val MAX_NICKNAME_GENERATION_ATTEMPTS = 100
    }

    @Transactional
    override fun sendSignupEmailVerificationCode(email: String) {
        val emailVO = Email(email)

        if (memberRepository.existsByEmail(emailVO)) {
            throw BusinessException(ErrorCode.DUPLICATE_EMAIL)
        }

        verificationApi.createSignupEmailVerificationCode(emailVO)
    }

    @Transactional
    override fun verifySignupEmailCode(email: String, code: String) {
        verificationApi.verifySignupEmailVerificationCode(
            email = Email(email),
            code = code,
        )
    }

    @Transactional
    override fun signup(command: SignupCommand): Long {
        val emailVO = Email(command.email)

        if (memberRepository.existsByEmail(emailVO)) {
            throw BusinessException(ErrorCode.DUPLICATE_EMAIL)
        }

        if (memberRepository.existsByPhoneNumber(command.phoneNumber)) {
            throw BusinessException(ErrorCode.DUPLICATE_PHONE_NUMBER)
        }

        verificationApi.assertVerifiedSignupEmail(emailVO)

        val member = Member.create(
                email = emailVO,
                passwordHash = passwordEncoder.encode(command.password),
                name = command.name,
                nickname = generateUniqueNickname(),
                phoneNumber = command.phoneNumber,
                registeredAt = LocalDateTime.now(),
            )

        return memberRepository.save(member).id
    }

    @Transactional
    override fun authenticate(email: Email, password: String): AuthenticatedMember {
        val member = memberRepository.findByEmail(email)
            ?: throw AuthException(ErrorCode.INVALID_CREDENTIALS)

        if (!member.verifyPassword(password, passwordEncoder)) {
            throw AuthException(ErrorCode.INVALID_CREDENTIALS)
        }

        member.recordLogin(LocalDateTime.now())

        return AuthenticatedMember(member.id, member.email, member.role.name)
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): AuthenticatedMember? {
        return memberRepository.findById(id)
            ?.let { member -> AuthenticatedMember(member.id, member.email, member.role.name) }
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
}