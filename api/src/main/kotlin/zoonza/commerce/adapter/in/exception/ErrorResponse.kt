package zoonza.commerce.adapter.`in`.exception

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.validation.BindingResult
import zoonza.commerce.exception.ErrorCode

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val code: String,
    val message: String,
    val errors: List<FieldError>? = null,
) {
    data class FieldError(
        val field: String,
        val value: String,
        val reason: String,
    )

    companion object {
        fun of(errorCode: ErrorCode): ErrorResponse =
            ErrorResponse(
                code = errorCode.code,
                message = errorCode.message,
            )

        fun of(errorCode: ErrorCode, message: String): ErrorResponse =
            ErrorResponse(
                code = errorCode.code,
                message = message,
            )

        fun of(errorCode: ErrorCode, bindingResult: BindingResult): ErrorResponse =
            ErrorResponse(
                code = errorCode.code,
                message = errorCode.message,
                errors = bindingResult.fieldErrors.map { fieldError ->
                    FieldError(
                        field = fieldError.field,
                        value = fieldError.rejectedValue?.toString() ?: "",
                        reason = fieldError.defaultMessage ?: "",
                    )
                },
            )
    }
}
