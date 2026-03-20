package zoonza.commerce.auth.adapter.out

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

class OpaqueRefreshTokenProviderTest {
    private val refreshTokenProvider = OpaqueRefreshTokenProvider(1_200_000)

    @Test
    fun `refresh 토큰은 비어 있지 않고 만료 시간이 발급 시간보다 뒤에 있다`() {
        val refreshToken = refreshTokenProvider.issue()

        refreshToken.token.shouldNotBeBlank()
        refreshToken.expiresAt.isAfter(refreshToken.issuedAt) shouldBe true
    }

    @Test
    fun `refresh 토큰은 매번 새로운 값을 발급한다`() {
        val first = refreshTokenProvider.issue()
        val second = refreshTokenProvider.issue()

        first.token shouldNotBe second.token
    }
}
