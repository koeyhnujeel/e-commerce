package zoonza.commerce.support.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import zoonza.commerce.shared.AuthException
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.ErrorStatus

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = e.errorCode

        return ResponseEntity
            .status(errorCode.status.toHttpStatus())
            .body(ApiResponse.error(ErrorResponse.of(errorCode, e.message)))
    }

    @ExceptionHandler(AuthException::class)
    fun handleAuthException(e: AuthException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(e.errorCode.status.toHttpStatus())
            .body(ApiResponse.error(ErrorResponse.of(e.errorCode, e.message)))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ErrorResponse.of(WebErrorCode.INVALID_INPUT_VALUE, e.bindingResult)))
    }

    private fun ErrorStatus.toHttpStatus(): HttpStatus {
        return when (this) {
            ErrorStatus.BAD_REQUEST -> HttpStatus.BAD_REQUEST
            ErrorStatus.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED
            ErrorStatus.CONFLICT -> HttpStatus.CONFLICT
            ErrorStatus.NOT_FOUND -> HttpStatus.NOT_FOUND
            ErrorStatus.BAD_GATEWAY -> HttpStatus.BAD_GATEWAY
        }
    }
}
