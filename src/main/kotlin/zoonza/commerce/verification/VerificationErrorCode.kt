package zoonza.commerce.verification

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class VerificationErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    EMAIL_VERIFICATION_NOT_FOUND(ErrorStatus.NOT_FOUND, "이메일 인증 요청을 찾을 수 없습니다."),
    EMAIL_NOT_VERIFIED(ErrorStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다."),
    INVALID_VERIFICATION_CODE(ErrorStatus.BAD_REQUEST, "인증 코드가 올바르지 않습니다."),
    EXPIRED_VERIFICATION_CODE(ErrorStatus.BAD_REQUEST, "인증 코드가 만료되었습니다.");

    override val code: String
        get() = status.name
}
