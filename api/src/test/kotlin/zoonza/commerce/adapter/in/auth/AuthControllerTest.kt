package zoonza.commerce.adapter.`in`.auth

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.adapter.`in`.auth.request.LoginRequest
import zoonza.commerce.adapter.out.persistence.auth.RefreshTokenJpaRepository
import zoonza.commerce.adapter.out.persistence.member.MemberJapRepository
import zoonza.commerce.auth.RefreshToken
import zoonza.commerce.auth.port.out.TokenProvider
import zoonza.commerce.common.Email
import zoonza.commerce.member.Member
import zoonza.commerce.member.Role
import zoonza.commerce.support.MySqlTestContainerConfig
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
    private lateinit var memberJpaRepository: MemberJapRepository

    @Autowired
    private lateinit var refreshTokenJpaRepository: RefreshTokenJpaRepository

    @Autowired
    private lateinit var tokenProvider: TokenProvider

    private val passwordEncoder = BCryptPasswordEncoder()

    @Test
    fun `로그인 유지가 선택되면 access 토큰과 refresh 쿠키를 발급한다`() {
        val member = insertMember(rawPassword = "Password123!")
        val request =
            LoginRequest(
                email = member.email.address,
                password = "Password123!",
                rememberMe = true,
            )

        val result =
            mockMvc
                .post("/api/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.data.accessToken") { exists() }
                    jsonPath("$.error") { doesNotExist() }
                }.andReturn()

        val setCookie = result.response.getHeader("Set-Cookie")
        val savedRefreshToken = refreshTokenJpaRepository.findByMemberId(member.id)
        val updatedMember = memberJpaRepository.findById(member.id).orElseThrow()

        setCookie.shouldNotBeNull()
        setCookie.contains("refreshToken=") shouldBe true
        setCookie.contains("HttpOnly") shouldBe true
        savedRefreshToken.shouldNotBeNull()
        extractRefreshTokenValue(setCookie) shouldBe savedRefreshToken.token
        updatedMember.lastLoginAt.shouldNotBeNull()
    }

    @Test
    fun `로그인 유지가 선택되지 않으면 기존 refresh 토큰을 제거하고 refreshToken 쿠키 삭제 응답을 반환한다`() {
        val member = insertMember(rawPassword = "Password123!")
        val refreshToken = tokenProvider.generateRefreshToken(member.id)
        refreshTokenJpaRepository.save(
            RefreshToken.issue(
                memberId = member.id,
                token = refreshToken.token,
                issuedAt = refreshToken.issuedAt,
                expiresAt = refreshToken.expiresAt,
            ),
        )
        val request =
            LoginRequest(
                email = member.email.address,
                password = "Password123!",
                rememberMe = false,
            )

        val result =
            mockMvc
                .post("/api/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.data.accessToken") { exists() }
                }.andReturn()

        val setCookie = result.response.getHeader("Set-Cookie")

        setCookie.shouldNotBeNull()
        setCookie.contains("Max-Age=0") shouldBe true
        refreshTokenJpaRepository.findByMemberId(member.id).shouldBeNull()
    }

    @Test
    fun `로그인 유지가 선택되지 않으면 refresh 토큰 없이 로그인하고 refreshToken 쿠키 삭제 응답을 반환한다`() {
        val member = insertMember(rawPassword = "Password123!")
        val request =
            LoginRequest(
                email = member.email.address,
                password = "Password123!",
                rememberMe = false,
            )

        val result =
            mockMvc
                .post("/api/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.data.accessToken") { exists() }
                }.andReturn()

        val setCookie = result.response.getHeader("Set-Cookie")
        val updatedMember = memberJpaRepository.findById(member.id).orElseThrow()

        setCookie.shouldNotBeNull()
        setCookie.contains("Max-Age=0") shouldBe true
        refreshTokenJpaRepository.findByMemberId(member.id).shouldBeNull()
        updatedMember.lastLoginAt.shouldNotBeNull()
    }

    @Test
    fun `이메일이나 비밀번호가 올바르지 않으면 인증 실패 응답을 반환한다`() {
        val member = insertMember(rawPassword = "Password123!")
        val request =
            LoginRequest(
                email = member.email.address,
                password = "WrongPassword123!",
                rememberMe = true,
            )

        mockMvc
            .post("/api/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnauthorized() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("UNAUTHORIZED") }
                jsonPath("$.error.message") { value("이메일 또는 비밀번호가 올바르지 않습니다.") }
            }
    }

    @Test
    fun `로그인 요청의 이메일 형식이 올바르지 않으면 한국어 검증 메시지를 반환한다`() {
        val request =
            LoginRequest(
                email = "invalid-email",
                password = "Password123!",
                rememberMe = false,
            )

        val result =
            mockMvc
                .post("/api/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.success") { value(false) }
                    jsonPath("$.error.code") { value("BAD_REQUEST") }
                }.andReturn()

        extractFieldErrors(result.response.contentAsString)["email"] shouldBe
            listOf("이메일 형식이 올바르지 않습니다.")
    }

    @Test
    fun `로그인 요청의 비밀번호가 비어 있으면 한국어 검증 메시지를 반환한다`() {
        val request =
            LoginRequest(
                email = "member@example.com",
                password = "",
                rememberMe = false,
            )

        val result =
            mockMvc
                .post("/api/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.success") { value(false) }
                    jsonPath("$.error.code") { value("BAD_REQUEST") }
                }.andReturn()

        extractFieldErrors(result.response.contentAsString)["password"] shouldBe
            listOf("비밀번호는 필수입니다.")
    }

    @Test
    fun `refresh 토큰이 유효하면 access 토큰과 refresh 쿠키를 모두 재발급한다`() {
        val member = insertMember(rawPassword = "Password123!", role = Role.SELLER)
        val oldRefreshToken = tokenProvider.generateRefreshToken(member.id)
        refreshTokenJpaRepository.save(
            RefreshToken.issue(
                memberId = member.id,
                token = oldRefreshToken.token,
                issuedAt = oldRefreshToken.issuedAt,
                expiresAt = oldRefreshToken.expiresAt,
            ),
        )

        val result =
            mockMvc
                .post("/api/auth/refresh") {
                    cookie(Cookie(RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME, oldRefreshToken.token))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.data.accessToken") { exists() }
                }.andReturn()

        val setCookie = result.response.getHeader("Set-Cookie")
        val rotatedRefreshToken = refreshTokenJpaRepository.findByMemberId(member.id)

        setCookie.shouldNotBeNull()
        rotatedRefreshToken.shouldNotBeNull()
        rotatedRefreshToken.token shouldBe extractRefreshTokenValue(setCookie)
        (rotatedRefreshToken.token == oldRefreshToken.token) shouldBe false
    }

    @Test
    fun `로그아웃 시 refresh 토큰을 삭제하고 refreshToken 쿠키 삭제 응답을 반환한다`() {
        val member = insertMember(rawPassword = "Password123!")
        val refreshToken = tokenProvider.generateRefreshToken(member.id)
        refreshTokenJpaRepository.save(
            RefreshToken.issue(
                memberId = member.id,
                token = refreshToken.token,
                issuedAt = refreshToken.issuedAt,
                expiresAt = refreshToken.expiresAt,
            ),
        )

        val result =
            mockMvc
                .post("/api/auth/logout") {
                    cookie(Cookie(RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME, refreshToken.token))
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.data") { doesNotExist() }
                    jsonPath("$.error") { doesNotExist() }
                }.andReturn()

        val setCookie = result.response.getHeader("Set-Cookie")

        setCookie.shouldNotBeNull()
        setCookie.contains("Max-Age=0") shouldBe true
        refreshTokenJpaRepository.findByMemberId(member.id).shouldBeNull()
    }

    @Test
    fun `로그아웃 시 refresh 토큰이 없어도 refreshToken 쿠키 삭제 응답을 반환한다`() {
        val result =
            mockMvc
                .post("/api/auth/logout")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.data") { doesNotExist() }
                    jsonPath("$.error") { doesNotExist() }
                }.andReturn()

        val setCookie = result.response.getHeader("Set-Cookie")

        setCookie.shouldNotBeNull()
        setCookie.contains("Max-Age=0") shouldBe true
    }

    @Test
    fun `로그아웃 시 잘못된 Authorization 헤더가 있어도 refresh 토큰을 삭제한다`() {
        val member = insertMember(rawPassword = "Password123!")
        val refreshToken = tokenProvider.generateRefreshToken(member.id)
        refreshTokenJpaRepository.save(
            RefreshToken.issue(
                memberId = member.id,
                token = refreshToken.token,
                issuedAt = refreshToken.issuedAt,
                expiresAt = refreshToken.expiresAt,
            ),
        )

        val result =
            mockMvc
                .post("/api/auth/logout") {
                    cookie(Cookie(RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME, refreshToken.token))
                    header("Authorization", "Bearer invalid-access-token")
                }.andExpect {
                    status { isOk() }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.data") { doesNotExist() }
                    jsonPath("$.error") { doesNotExist() }
                }.andReturn()

        val setCookie = result.response.getHeader("Set-Cookie")

        setCookie.shouldNotBeNull()
        setCookie.contains("Max-Age=0") shouldBe true
        refreshTokenJpaRepository.findByMemberId(member.id).shouldBeNull()
    }

    @Test
    fun `DB에 저장된 refresh 토큰이 없으면 인증 실패 응답과 refreshToken 쿠키 삭제 응답을 반환한다`() {
        val member = insertMember(rawPassword = "Password123!")
        val refreshToken = tokenProvider.generateRefreshToken(member.id)

        val result =
            mockMvc
                .post("/api/auth/refresh") {
                    cookie(Cookie(RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME, refreshToken.token))
                }.andExpect {
                    status { isUnauthorized() }
                    jsonPath("$.success") { value(false) }
                    jsonPath("$.error.code") { value("UNAUTHORIZED") }
                    jsonPath("$.error.message") { value("인증 토큰이 올바르지 않습니다.") }
                }.andReturn()

        val setCookie = result.response.getHeader("Set-Cookie")

        setCookie.shouldNotBeNull()
        setCookie.contains("Max-Age=0") shouldBe true
    }

    private fun insertMember(
        email: String = "member-${System.nanoTime()}@example.com",
        rawPassword: String,
        role: Role = Role.CUSTOMER,
    ): Member {
        return memberJpaRepository.save(
            Member.create(
                email = Email(email),
                passwordHash = passwordEncoder.encode(rawPassword),
                name = "홍길동",
                nickname = "tester-${System.nanoTime()}",
                phoneNumber = "010${(10000000..99999999).random()}",
                role = role,
                registeredAt = LocalDateTime.now(),
            ),
        )
    }

    private fun extractRefreshTokenValue(setCookie: String): String {
        return setCookie.substringAfter("refreshToken=").substringBefore(";")
    }

    private fun extractFieldErrors(content: String): Map<String, List<String>> {
        return objectMapper
            .readTree(content)
            .path("error")
            .path("errors")
            .associateBy(
                { it.path("field").asText() },
                { listOf(it.path("reason").asText()) },
            )
    }
}
