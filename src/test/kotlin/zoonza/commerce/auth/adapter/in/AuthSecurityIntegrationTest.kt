package zoonza.commerce.auth.adapter.`in`

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import zoonza.commerce.support.MySqlTestContainerConfig
import java.time.Instant
import java.util.Date

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MySqlTestContainerConfig::class)
class AuthSecurityIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `인증 정보가 없으면 RestAuthenticationEntryPoint가 401 응답을 반환한다`() {
        mockMvc
            .post("/api/products/1/likes")
            .andExpect {
                status { isUnauthorized() }
                header { string(HttpHeaders.WWW_AUTHENTICATE, "Bearer") }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("UNAUTHORIZED") }
            }
    }

    @Test
    fun `인증 정보가 없으면 주문 생성 엔드포인트도 401 응답을 반환한다`() {
        mockMvc
            .post("/api/orders")
            .andExpect {
                status { isUnauthorized() }
                header { string(HttpHeaders.WWW_AUTHENTICATE, "Bearer") }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("UNAUTHORIZED") }
            }
    }

    @Test
    fun `형식이 잘못된 access 토큰이면 401 응답을 반환한다`() {
        mockMvc
            .post("/api/products/1/likes") {
                header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
            }.andExpect {
                status { isUnauthorized() }
                header { string(HttpHeaders.WWW_AUTHENTICATE, "Bearer") }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.message") { value("인증 토큰이 올바르지 않습니다.") }
            }
    }

    @Test
    fun `만료된 access 토큰이면 401 응답을 반환한다`() {
        mockMvc
            .post("/api/products/1/likes") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${expiredAccessToken()}")
            }.andExpect {
                status { isUnauthorized() }
                header { string(HttpHeaders.WWW_AUTHENTICATE, "Bearer") }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.message") { value("인증 토큰이 만료되었습니다.") }
            }
    }

    private fun expiredAccessToken(): String {
        val key = Keys.hmacShaKeyFor(TEST_SECRET.toByteArray())
        val issuedAt = Instant.now().minusSeconds(120)
        val expiresAt = Instant.now().minusSeconds(60)

        return Jwts.builder()
            .subject("1")
            .claim("email", "member@example.com")
            .claim("role", "CUSTOMER")
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .signWith(key)
            .compact()
    }

    companion object {
        private const val TEST_SECRET = "test-secret-key-test-secret-key-123456"
    }
}
