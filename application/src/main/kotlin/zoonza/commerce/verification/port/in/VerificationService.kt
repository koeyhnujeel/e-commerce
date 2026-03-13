package zoonza.commerce.verification.port.`in`

import zoonza.commerce.common.Email
import zoonza.commerce.verification.VerificationPurpose

interface VerificationService {
    fun issueEmailVerification(email: Email, purpose: VerificationPurpose)
}
