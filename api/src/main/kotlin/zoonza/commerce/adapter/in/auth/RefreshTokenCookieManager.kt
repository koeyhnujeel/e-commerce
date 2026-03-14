package zoonza.commerce.adapter.`in`.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RefreshTokenCookieManager(
    @Value("\${jwt.refresh-expiration-ms}")
    private val refreshExpirationMs: Long,
) {
    fun create(token: String): ResponseCookie {
        return ResponseCookie
            .from(REFRESH_TOKEN_COOKIE_NAME, token)
            .httpOnly(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(Duration.ofMillis(refreshExpirationMs))
            .build()
    }

    fun expire(): ResponseCookie {
        return ResponseCookie
            .from(REFRESH_TOKEN_COOKIE_NAME, "")
            .httpOnly(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(Duration.ZERO)
            .build()
    }

    companion object {
        const val REFRESH_TOKEN_COOKIE_NAME = "refreshToken"
    }
}
