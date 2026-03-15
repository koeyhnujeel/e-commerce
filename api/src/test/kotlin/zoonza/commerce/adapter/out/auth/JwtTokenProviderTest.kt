package zoonza.commerce.adapter.out.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import zoonza.commerce.auth.dto.AccessTokenClaims
import zoonza.commerce.exception.AuthException
import zoonza.commerce.exception.ErrorCode
import zoonza.commerce.member.Role
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.test.Test

class JwtTokenProviderTest {
    private val secret = "test-secret"
    private val tokenProvider = JwtTokenProvider(secret, 600_000, 1_200_000)

    @Test
    fun `access 토큰에 회원 정보와 역할 클레임을 담는다`() {
        val accessToken = tokenProvider.generateAccessToken(1L, "member@example.com", Role.SELLER)

        val claims = parseClaims(accessToken.token)

        claims.subject shouldBe "1"
        claims["email"] shouldBe "member@example.com"
        claims["role"] shouldBe "SELLER"
        claims["type"] shouldBe "access"
    }

    @Test
    fun `access 토큰을 파싱하면 회원 정보와 역할을 얻을 수 있다`() {
        val accessToken = tokenProvider.generateAccessToken(1L, "member@example.com", Role.SELLER)

        tokenProvider.validateAccessToken(accessToken.token)
        val claims = tokenProvider.parseAccessToken(accessToken.token)

        claims shouldBe AccessTokenClaims(1L, "member@example.com", Role.SELLER)
    }

    @Test
    fun `refresh 토큰을 파싱하면 회원 식별자를 얻을 수 있다`() {
        val refreshToken = tokenProvider.generateRefreshToken(1L)

        tokenProvider.validateRefreshToken(refreshToken.token)
        val claims = tokenProvider.parseRefreshToken(refreshToken.token)

        claims.memberId shouldBe 1L
    }

    @Test
    fun `access 토큰을 refresh 토큰으로 검증하면 예외를 던진다`() {
        val accessToken = tokenProvider.generateAccessToken(1L, "member@example.com", Role.CUSTOMER)

        val exception =
            shouldThrow<AuthException> {
                tokenProvider.validateRefreshToken(accessToken.token)
            }

        exception.errorCode shouldBe ErrorCode.INVALID_TOKEN
    }

    @Test
    fun `refresh 토큰을 access 토큰으로 검증하면 예외를 던진다`() {
        val refreshToken = tokenProvider.generateRefreshToken(1L)

        val exception =
            shouldThrow<AuthException> {
                tokenProvider.validateAccessToken(refreshToken.token)
            }

        exception.errorCode shouldBe ErrorCode.INVALID_TOKEN
    }

    @Test
    fun `서명이 다른 refresh 토큰을 검증하면 예외를 던진다`() {
        val otherTokenProvider = JwtTokenProvider("other-secret", 600_000, 1_200_000)
        val refreshToken = otherTokenProvider.generateRefreshToken(1L)

        val exception =
            shouldThrow<AuthException> {
                tokenProvider.validateRefreshToken(refreshToken.token)
            }

        exception.errorCode shouldBe ErrorCode.INVALID_TOKEN
    }

    @Test
    fun `만료된 refresh 토큰을 검증하면 예외를 던진다`() {
        val expiredTokenProvider = JwtTokenProvider(secret, 600_000, -1)
        val refreshToken = expiredTokenProvider.generateRefreshToken(1L)

        val exception =
            shouldThrow<AuthException> {
                expiredTokenProvider.validateRefreshToken(refreshToken.token)
            }

        exception.errorCode shouldBe ErrorCode.EXPIRED_TOKEN
    }

    private fun parseClaims(token: String): Claims {
        return Jwts
            .parser()
            .verifyWith(Keys.hmacShaKeyFor(secretBytes(secret)))
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun secretBytes(secret: String): ByteArray {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(secret.toByteArray(StandardCharsets.UTF_8))
    }
}
