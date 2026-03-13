package zoonza.commerce.exception

enum class ErrorCode(
    val code: String,
    val message: String,
) {
    INVALID_INPUT_VALUE("BAD_REQUEST", "입력값이 올바르지 않습니다."),
    DUPLICATE_EMAIL("CONFLICT", "이미 사용 중인 이메일입니다.")
}