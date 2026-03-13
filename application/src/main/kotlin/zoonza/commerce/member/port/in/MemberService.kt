package zoonza.commerce.member.port.`in`

interface MemberService {
    fun sendSignupEmailVerificationCode(email: String)

    fun verifySignupEmailCode(email: String, code: String)

//    fun checkPhoneNumberAvailability(phoneNumber: String)
}
