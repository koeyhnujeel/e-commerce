package zoonza.commerce.member.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.common.Email
import zoonza.commerce.exception.BusinessException
import zoonza.commerce.exception.ErrorCode
import zoonza.commerce.member.port.`in`.MemberService
import zoonza.commerce.member.port.out.MemberRepository
import zoonza.commerce.verification.VerificationPurpose
import zoonza.commerce.verification.port.`in`.VerificationService

@Service
class DefaultMemberService(
    private val memberRepository: MemberRepository,
    private val verificationService: VerificationService,
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
}
