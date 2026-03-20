package zoonza.commerce.verification

import zoonza.commerce.shared.Email

interface VerificationApi {
    fun createSignupEmailVerificationCode(email: Email)

    fun verifySignupEmailVerificationCode(email: Email, code: String)

    fun assertVerifiedSignupEmail(email: Email)
}