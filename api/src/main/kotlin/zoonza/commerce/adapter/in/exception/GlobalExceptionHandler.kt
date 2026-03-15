package zoonza.commerce.adapter.`in`.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import zoonza.commerce.adapter.`in`.auth.RefreshTokenCookieManager
import zoonza.commerce.adapter.`in`.response.ApiResponse
import zoonza.commerce.exception.AuthException
import zoonza.commerce.exception.BusinessException
import zoonza.commerce.exception.ErrorCode

@RestControllerAdvice
class GlobalExceptionHandler(
    private val refreshTokenCookieManager: RefreshTokenCookieManager,
) {
    private val businessStatusMap: Map<ErrorCode, HttpStatus> =
        mapOf(
            ErrorCode.INVALID_INPUT_VALUE to HttpStatus.BAD_REQUEST,
            ErrorCode.DUPLICATE_EMAIL to HttpStatus.CONFLICT,
            ErrorCode.DUPLICATE_PHONE_NUMBER to HttpStatus.CONFLICT,
            ErrorCode.EMAIL_VERIFICATION_NOT_FOUND to HttpStatus.NOT_FOUND,
            ErrorCode.EMAIL_NOT_VERIFIED to HttpStatus.BAD_REQUEST,
            ErrorCode.INVALID_VERIFICATION_CODE to HttpStatus.BAD_REQUEST,
            ErrorCode.EXPIRED_VERIFICATION_CODE to HttpStatus.BAD_REQUEST,
            ErrorCode.PRODUCT_NOT_FOUND to HttpStatus.NOT_FOUND,
        )

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        e: BusinessException,
    ): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = e.errorCode

        return ResponseEntity
            .status(businessStatusMap[errorCode] ?: HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ErrorResponse.of(errorCode, e.message)))
    }

    @ExceptionHandler(AuthException::class)
    fun handleAuthException(
        e: AuthException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<ApiResponse<Nothing>> {
        if (shouldExpireRefreshTokenCookie(request.requestURI, e.errorCode)) {
            response.addHeader("Set-Cookie", refreshTokenCookieManager.expire().toString())
        }

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ErrorResponse.of(e.errorCode, e.message)))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        e: MethodArgumentNotValidException,
    ): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.bindingResult)))
    }

    private fun shouldExpireRefreshTokenCookie(
        requestUri: String,
        errorCode: ErrorCode,
    ): Boolean {
        if (requestUri != "/api/auth/refresh") {
            return false
        }

        return errorCode == ErrorCode.INVALID_TOKEN ||
            errorCode == ErrorCode.EXPIRED_TOKEN ||
            errorCode == ErrorCode.UNAUTHORIZED
    }
}
