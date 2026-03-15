package zoonza.commerce.adapter.`in`.auth

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.adapter.`in`.auth.request.LoginRequest
import zoonza.commerce.adapter.`in`.auth.response.AuthTokenResponse
import zoonza.commerce.adapter.`in`.response.ApiResponse
import zoonza.commerce.auth.dto.LoginCommand
import zoonza.commerce.auth.port.`in`.AuthService
import zoonza.commerce.exception.AuthException
import zoonza.commerce.exception.ErrorCode

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
    ): ApiResponse<AuthTokenResponse> {
        val result = authService.login(
                LoginCommand(
                    email = request.email,
                    password = request.password,
                    rememberMe = request.rememberMe,
                ),
            )

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            result.refreshToken?.let(refreshTokenCookieManager::create)?.toString()
                ?: refreshTokenCookieManager.expire().toString(),
        )

        return ApiResponse.success(AuthTokenResponse(accessToken = result.accessToken))
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(name = RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME, required = false)
        refreshToken: String?,
        response: HttpServletResponse,
    ): ApiResponse<AuthTokenResponse> {
        val result = authService.reissueAccessToken(
                refreshToken ?: throw AuthException(ErrorCode.UNAUTHORIZED),
            )

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            refreshTokenCookieManager.create(result.refreshToken).toString(),
        )

        return ApiResponse.success(AuthTokenResponse(accessToken = result.accessToken))
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
