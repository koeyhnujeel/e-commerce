package zoonza.commerce.exception

enum class ErrorCode(
    val code: String,
    val message: String,
) {
    INVALID_INPUT_VALUE("BAD_REQUEST", "입력값이 올바르지 않습니다.")
}