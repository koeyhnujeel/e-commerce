package zoonza.commerce.shared

enum class AuthErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    INVALID_CREDENTIALS(ErrorStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(ErrorStatus.UNAUTHORIZED, "인증 토큰이 올바르지 않습니다."),
    EXPIRED_TOKEN(ErrorStatus.UNAUTHORIZED, "인증 토큰이 만료되었습니다."),
    UNAUTHORIZED(ErrorStatus.UNAUTHORIZED, "인증이 필요합니다.");

    override val code: String
        get() = status.name
}
