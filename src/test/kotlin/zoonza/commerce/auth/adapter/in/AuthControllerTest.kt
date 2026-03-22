package zoonza.commerce.auth.adapter.`in`

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.auth.adapter.`in`.request.LoginRequest
import zoonza.commerce.auth.adapter.out.persistence.RefreshTokenJpaRepository
import zoonza.commerce.member.adapter.out.persistence.MemberJapRepository
import zoonza.commerce.member.domain.PasswordEncoder
import zoonza.commerce.support.MySqlTestContainerConfig
import zoonza.commerce.support.fixture.MemberFixture
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(MySqlTestContainerConfig::class)
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var memberJapRepository: MemberJapRepository

    @Autowired
    private lateinit var refreshTokenJpaRepository: RefreshTokenJpaRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun `로그인에 성공하면 access 토큰과 refresh 토큰 쿠키를 반환한다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.create(
                    email = "member@example.com",
                    passwordHash = passwordEncoder.encode("Password123!"),
                    name = "주문자",
                    nickname = "nickname",
                    phoneNumber = "01012345678",
                    registeredAt = LocalDateTime.now(),
                ),
            )

        val result =
            mockMvc
                .post("/api/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(
                        LoginRequest(
                            email = "member@example.com",
                            password = "Password123!",
                        ),
                    )
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.data.accessToken") { isString() }
                }.andReturn()

        val setCookie = result.response.getHeader(HttpHeaders.SET_COOKIE)
        setCookie.shouldNotBeNull()
        extractRefreshToken(setCookie).shouldNotBeNull()
        refreshTokenJpaRepository.findByMemberId(member.id).shouldNotBeNull()
    }

    @Test
    fun `로그인 정보가 잘못되면 인증 실패 응답을 반환한다`() {
        memberJapRepository.save(
            MemberFixture.create(
                email = "member@example.com",
                passwordHash = passwordEncoder.encode("Password123!"),
                name = "주문자",
                nickname = "nickname",
                phoneNumber = "01012345678",
                registeredAt = LocalDateTime.now(),
            ),
        )

        mockMvc
            .post("/api/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    LoginRequest(
                        email = "member@example.com",
                        password = "WrongPassword123!",
                    ),
                )
            }.andExpect {
                status { isUnauthorized() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("UNAUTHORIZED") }
            }
    }

    @Test
    fun `refresh 쿠키가 있으면 access 토큰과 refresh 토큰을 다시 발급한다`() {
        val member =
            memberJapRepository.save(
                MemberFixture.create(
                    email = "member@example.com",
                    passwordHash = passwordEncoder.encode("Password123!"),
                    name = "주문자",
                    nickname = "nickname",
                    phoneNumber = "01012345678",
                    registeredAt = LocalDateTime.now(),
                ),
            )
        val loginResult =
            mockMvc
                .post("/api/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(
                        LoginRequest("member@example.com", "Password123!"),
                    )
                }.andReturn()
        val originalRefreshToken = extractRefreshToken(loginResult.response.getHeader(HttpHeaders.SET_COOKIE)!!)

        val refreshResult =
            mockMvc
                .post("/api/auth/refresh") {
                    cookie(Cookie(RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME, originalRefreshToken))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.data.accessToken") { isString() }
                }.andReturn()

        val rotatedRefreshToken = extractRefreshToken(refreshResult.response.getHeader(HttpHeaders.SET_COOKIE)!!)
        rotatedRefreshToken shouldBe refreshTokenJpaRepository.findByMemberId(member.id)?.token
    }

    @Test
    fun `refresh 쿠키가 없으면 인증 실패 응답을 반환한다`() {
        mockMvc
            .post("/api/auth/refresh")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("UNAUTHORIZED") }
            }
    }

    @Test
    fun `logout하면 refresh 토큰을 삭제하고 만료 쿠키를 반환한다`() {
        memberJapRepository.save(
            MemberFixture.create(
                email = "member@example.com",
                passwordHash = passwordEncoder.encode("Password123!"),
                name = "주문자",
                nickname = "nickname",
                phoneNumber = "01012345678",
                registeredAt = LocalDateTime.now(),
            ),
        )
        val loginResult =
            mockMvc
                .post("/api/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(
                        LoginRequest("member@example.com", "Password123!"),
                    )
                }.andReturn()
        val refreshToken = extractRefreshToken(loginResult.response.getHeader(HttpHeaders.SET_COOKIE)!!)

        val logoutResult =
            mockMvc
                .post("/api/auth/logout") {
                    cookie(Cookie(RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME, refreshToken))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                }.andReturn()

        val expiredCookie = logoutResult.response.getHeader(HttpHeaders.SET_COOKIE)
        expiredCookie.shouldNotBeNull()
        expiredCookie.contains("Max-Age=0") shouldBe true
        refreshTokenJpaRepository.findByToken(refreshToken) shouldBe null
    }
    private fun extractRefreshToken(setCookie: String): String {
        return Regex("""${RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME}=([^;]+)""")
            .find(setCookie)
            ?.groupValues
            ?.get(1)
            ?: error("refresh token cookie not found")
    }
}
