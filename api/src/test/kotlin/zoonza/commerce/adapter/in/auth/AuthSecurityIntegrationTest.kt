package zoonza.commerce.adapter.`in`.auth

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import zoonza.commerce.adapter.`in`.response.ApiResponse
import zoonza.commerce.auth.port.out.TokenProvider
import zoonza.commerce.member.Role
import zoonza.commerce.support.MySqlTestContainerConfig

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MySqlTestContainerConfig::class, AuthSecurityIntegrationTest.TestConfig::class)
class AuthSecurityIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var tokenProvider: TokenProvider

    @Test
    fun `유효한 access 토큰이 있으면 SecurityContext에 인증 정보를 채운다`() {
        val accessToken = tokenProvider.generateAccessToken(1L, "member@example.com", Role.ADMIN)

        mockMvc
            .get("/api/test/authenticated") {
                header("Authorization", "Bearer ${accessToken.token}")
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.memberId") { value(1) }
                jsonPath("$.data.email") { value("member@example.com") }
                jsonPath("$.data.role") { value("ADMIN") }
            }
    }

    @Test
    fun `access 토큰이 없으면 인증 필요 응답을 반환한다`() {
        mockMvc
            .get("/api/test/authenticated")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("UNAUTHORIZED") }
                jsonPath("$.error.message") { value("인증이 필요합니다.") }
            }
    }

    @Test
    fun `Bearer prefix가 아닌 Authorization 헤더는 무시하고 인증 필요 응답을 반환한다`() {
        mockMvc
            .get("/api/test/authenticated") {
                header("Authorization", "Basic invalid-token")
            }.andExpect {
                status { isUnauthorized() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("UNAUTHORIZED") }
                jsonPath("$.error.message") { value("인증이 필요합니다.") }
            }
    }

    @Test
    fun `잘못된 access 토큰이면 인증 실패 응답을 반환한다`() {
        val accessToken = tokenProvider.generateAccessToken(1L, "member@example.com", Role.CUSTOMER)

        mockMvc
            .get("/api/test/authenticated") {
                header("Authorization", "Bearer ${accessToken.token}broken")
            }.andExpect {
                status { isUnauthorized() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("UNAUTHORIZED") }
                jsonPath("$.error.message") { value("인증 토큰이 올바르지 않습니다.") }
            }
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun testAuthenticatedController(): TestAuthenticatedController {
            return TestAuthenticatedController()
        }
    }
}

data class TestAuthenticatedResponse(
    val memberId: Long,
    val email: String,
    val role: String,
)

@RestController
class TestAuthenticatedController {
    @GetMapping("/api/test/authenticated")
    fun authenticated(
        @AuthenticationPrincipal authenticatedMember: AuthenticatedMember,
    ): ApiResponse<TestAuthenticatedResponse> {
        return ApiResponse.success(
            TestAuthenticatedResponse(
                memberId = authenticatedMember.memberId,
                email = authenticatedMember.email,
                role = authenticatedMember.role.name,
            ),
        )
    }
}
