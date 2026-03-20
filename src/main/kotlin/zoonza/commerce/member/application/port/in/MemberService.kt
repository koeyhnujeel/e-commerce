package zoonza.commerce.member.application.port.`in`

import zoonza.commerce.member.application.dto.SignupCommand

interface MemberService {
    fun sendSignupEmailVerificationCode(email: String)

    fun verifySignupEmailCode(email: String, code: String)

    fun signup(command: SignupCommand): Long
}
