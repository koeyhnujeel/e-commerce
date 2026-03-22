package zoonza.commerce.support.web

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class WebErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    INVALID_INPUT_VALUE(ErrorStatus.BAD_REQUEST, "입력값이 올바르지 않습니다.");

    override val code: String
        get() = status.name
}
