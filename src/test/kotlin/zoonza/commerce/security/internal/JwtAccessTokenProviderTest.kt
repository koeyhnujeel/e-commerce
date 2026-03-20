package zoonza.commerce.security.internal

import io.jsonwebtoken.JwtException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class JwtAccessTokenProviderTest {
    private val accessTokenProvider =
        JwtAccessTokenProvider(
            secret = "test-secret-key-test-secret-key-123456",
            accessTtlMs = 600_000,
        )

    @Test
    fun `access 토큰에 회원 정보를 담는다`() {
        val accessToken = accessTokenProvider.issue(1L, "member@example.com", "SELLER")

        val authMember = accessTokenProvider.parse(accessToken)

        authMember.memberId shouldBe 1L
        authMember.email shouldBe "member@example.com"
        authMember.role shouldBe "SELLER"
    }

    @Test
    fun `형식이 잘못된 토큰이면 예외를 던진다`() {
        shouldThrow<JwtException> {
            accessTokenProvider.parse("invalid-token")
        }
    }
}
