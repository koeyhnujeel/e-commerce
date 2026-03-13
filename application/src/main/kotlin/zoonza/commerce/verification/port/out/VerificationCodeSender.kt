package zoonza.commerce.verification.port.out

import zoonza.commerce.common.Email
import zoonza.commerce.verification.VerificationPurpose

interface VerificationCodeSender {
    fun sendVerificationCode(
        to: Email,
        purpose: VerificationPurpose,
        code: String,
    )
}
