package zoonza.commerce.member.port.`in`

import zoonza.commerce.member.dto.SignupCommand

interface MemberService {
    fun sendSignupEmailVerificationCode(email: String)

    fun verifySignupEmailCode(email: String, code: String)

    fun signup(command: SignupCommand): Long
}
