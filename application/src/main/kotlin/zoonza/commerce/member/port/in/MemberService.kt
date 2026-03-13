package zoonza.commerce.member.port.`in`

import zoonza.commerce.member.dto.SignUpCommand

interface MemberService {
    fun sendSignupEmailVerificationCode(email: String)

    fun verifySignupEmailCode(email: String, code: String)

    fun signUp(command: SignUpCommand): Long

//    fun checkPhoneNumberAvailability(phoneNumber: String)
}
