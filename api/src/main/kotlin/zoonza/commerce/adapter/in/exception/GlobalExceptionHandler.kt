package zoonza.commerce.adapter.`in`.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import zoonza.commerce.adapter.`in`.response.ApiResponse
import zoonza.commerce.exception.BusinessException
import zoonza.commerce.exception.ErrorCode

@RestControllerAdvice
class GlobalExceptionHandler {
    private val statusMap: Map<ErrorCode, HttpStatus> =
        mapOf(
            ErrorCode.INVALID_INPUT_VALUE to HttpStatus.BAD_REQUEST,
            ErrorCode.DUPLICATE_EMAIL to HttpStatus.CONFLICT,
        )

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = e.errorCode
        return ResponseEntity
            .status(statusMap[errorCode] ?: HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ErrorResponse.of(errorCode, e.message)))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        e: MethodArgumentNotValidException,
    ): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.bindingResult)))
    }
}
