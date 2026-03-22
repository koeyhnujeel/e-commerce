package zoonza.commerce.support.web

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.validation.BindingResult
import zoonza.commerce.shared.ErrorDescriptor

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
        fun of(errorCode: ErrorDescriptor): ErrorResponse =
            ErrorResponse(
                code = errorCode.code,
                message = errorCode.message,
            )

        fun of(errorCode: ErrorDescriptor, message: String): ErrorResponse =
            ErrorResponse(
                code = errorCode.code,
                message = message,
            )

        fun of(errorCode: ErrorDescriptor, bindingResult: BindingResult): ErrorResponse =
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
