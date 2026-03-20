package zoonza.commerce.auth.adapter.`in`

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*
import zoonza.commerce.auth.adapter.`in`.RefreshTokenCookieManager
import zoonza.commerce.auth.adapter.`in`.request.LoginRequest
import zoonza.commerce.auth.adapter.`in`.response.LoginResponse
import zoonza.commerce.auth.application.dto.LoginCommand
import zoonza.commerce.auth.application.port.`in`.AuthService
import zoonza.commerce.common.ApiResponse
import zoonza.commerce.shared.AuthException
import zoonza.commerce.shared.ErrorCode

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val refreshTokenCookieManager: RefreshTokenCookieManager,
) {
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        response: HttpServletResponse,
    ): ApiResponse<LoginResponse> {
        val command = LoginCommand(request.email, request.password)

        val result = authService.login(command)

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            result.refreshToken.let(refreshTokenCookieManager::create).toString()
        )

        return ApiResponse.success(LoginResponse(result.accessToken))
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(name = RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME, required = false)
        refreshToken: String?,
        response: HttpServletResponse,
    ): ApiResponse<LoginResponse> {
        val result = authService.refresh(
                refreshToken ?: throw AuthException(ErrorCode.UNAUTHORIZED),
            )

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            refreshTokenCookieManager.create(result.refreshToken).toString(),
        )

        return ApiResponse.success(LoginResponse(accessToken = result.accessToken))
    }

    @PostMapping("/logout")
    fun logout(
        @CookieValue(name = RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME, required = false)
        refreshToken: String?,
        response: HttpServletResponse,
    ): ApiResponse<Nothing> {
        authService.logout(refreshToken)

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            refreshTokenCookieManager.expire().toString(),
        )

        return ApiResponse.success()
    }
}
