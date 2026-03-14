package zoonza.commerce.auth.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import zoonza.commerce.auth.RefreshToken
import zoonza.commerce.auth.dto.IssuedToken
import zoonza.commerce.auth.dto.LoginCommand
import zoonza.commerce.auth.dto.RefreshTokenClaims
import zoonza.commerce.auth.port.out.RefreshTokenRepository
import zoonza.commerce.auth.port.out.TokenProvider
import zoonza.commerce.common.Email
import zoonza.commerce.exception.AuthException
import zoonza.commerce.exception.ErrorCode
import zoonza.commerce.member.Member
import zoonza.commerce.member.Role
import zoonza.commerce.member.port.out.MemberRepository
import zoonza.commerce.member.port.out.PasswordHasher
import java.time.LocalDateTime
import kotlin.test.Test

class DefaultAuthServiceTest {
    private val memberRepository = mockk<MemberRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val tokenProvider = mockk<TokenProvider>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val authService =
        DefaultAuthService(
            memberRepository = memberRepository,
            passwordHasher = passwordHasher,
            tokenProvider = tokenProvider,
            refreshTokenRepository = refreshTokenRepository,
        )

    @Test
    fun `로그인 유지가 선택되면 access 토큰과 refresh 토큰을 발급하고 저장한다`() {
        val member = createMember()
        val email = member.email
        val accessToken = issuedToken("access-token")
        val refreshToken = issuedToken("refresh-token")
        val savedRefreshToken = slot<RefreshToken>()

        every { memberRepository.findByEmail(email) } returns member
        every { passwordHasher.matches("Password123!", member.passwordHash) } returns true
        every { memberRepository.save(member) } returns member
        every {
            tokenProvider.generateAccessToken(member.id, member.email.address, member.role)
        } returns accessToken
        every { tokenProvider.generateRefreshToken(member.id) } returns refreshToken
        every { refreshTokenRepository.findByMemberId(member.id) } returns null
        every { refreshTokenRepository.save(capture(savedRefreshToken)) } answers { firstArg() }

        val result =
            authService.login(
                LoginCommand(
                    email = member.email.address,
                    password = "Password123!",
                    rememberMe = true,
                ),
            )

        result.accessToken shouldBe accessToken.token
        result.refreshToken shouldBe refreshToken.token
        member.lastLoginAt.shouldNotBeNull()
        savedRefreshToken.captured.memberId shouldBe member.id
        savedRefreshToken.captured.token shouldBe refreshToken.token
        verify(exactly = 1) { memberRepository.findByEmail(email) }
        verify(exactly = 1) { memberRepository.save(member) }
        verify(exactly = 1) { refreshTokenRepository.save(any()) }
        verify(exactly = 0) { refreshTokenRepository.deleteByMemberId(any()) }
    }

    @Test
    fun `로그인 유지가 선택되지 않으면 기존 refresh 토큰을 삭제한다`() {
        val member = createMember()
        val email = member.email
        val accessToken = issuedToken("access-token")

        every { memberRepository.findByEmail(email) } returns member
        every { passwordHasher.matches("Password123!", member.passwordHash) } returns true
        every { memberRepository.save(member) } returns member
        every {
            tokenProvider.generateAccessToken(member.id, member.email.address, member.role)
        } returns accessToken
        every { refreshTokenRepository.deleteByMemberId(member.id) } just runs

        val result =
            authService.login(
                LoginCommand(
                    email = member.email.address,
                    password = "Password123!",
                    rememberMe = false,
                ),
            )

        result.accessToken shouldBe accessToken.token
        result.refreshToken.shouldBeNull()
        verify(exactly = 1) { refreshTokenRepository.deleteByMemberId(member.id) }
        verify(exactly = 0) { tokenProvider.generateRefreshToken(any()) }
        verify(exactly = 0) { refreshTokenRepository.save(any()) }
    }

    @Test
    fun `이메일에 해당하는 회원이 없으면 로그인에 실패한다`() {
        every { memberRepository.findByEmail(Email("member@example.com")) } returns null

        val exception =
            shouldThrow<AuthException> {
                authService.login(
                    LoginCommand(
                        email = "member@example.com",
                        password = "Password123!",
                        rememberMe = false,
                    ),
                )
            }

        exception.errorCode shouldBe ErrorCode.INVALID_CREDENTIALS
        verify(exactly = 0) { passwordHasher.matches(any(), any()) }
    }

    @Test
    fun `비밀번호가 일치하지 않으면 로그인에 실패한다`() {
        val member = createMember()

        every { memberRepository.findByEmail(member.email) } returns member
        every { passwordHasher.matches("Password123!", member.passwordHash) } returns false

        val exception =
            shouldThrow<AuthException> {
                authService.login(
                    LoginCommand(
                        email = member.email.address,
                        password = "Password123!",
                        rememberMe = false,
                    ),
                )
            }

        exception.errorCode shouldBe ErrorCode.INVALID_CREDENTIALS
        verify(exactly = 0) { memberRepository.save(any()) }
    }

    @Test
    fun `refresh 토큰이 유효하면 access 토큰과 refresh 토큰을 모두 재발급한다`() {
        val member = createMember(role = Role.SELLER)
        val oldRefreshToken =
            RefreshToken.issue(
                memberId = member.id,
                token = "old-refresh-token",
                issuedAt = LocalDateTime.now().minusDays(1),
                expiresAt = LocalDateTime.now().plusDays(1),
            )
        val newAccessToken = issuedToken("new-access-token")
        val newRefreshToken = issuedToken("new-refresh-token")

        every { tokenProvider.parseRefreshToken(oldRefreshToken.token) } returns RefreshTokenClaims(member.id)
        every { refreshTokenRepository.findByMemberId(member.id) } returns oldRefreshToken
        every { memberRepository.findById(member.id) } returns member
        every {
            tokenProvider.generateAccessToken(member.id, member.email.address, member.role)
        } returns newAccessToken
        every { tokenProvider.generateRefreshToken(member.id) } returns newRefreshToken
        every { refreshTokenRepository.save(oldRefreshToken) } returns oldRefreshToken

        val result = authService.reissueAccessToken(oldRefreshToken.token)

        result.accessToken shouldBe newAccessToken.token
        result.refreshToken shouldBe newRefreshToken.token
        oldRefreshToken.token shouldBe newRefreshToken.token
        verify(exactly = 1) { refreshTokenRepository.save(oldRefreshToken) }
    }

    @Test
    fun `DB에 저장된 refresh 토큰과 다르면 재발급에 실패한다`() {
        val member = createMember()
        val storedRefreshToken =
            RefreshToken.issue(
                memberId = member.id,
                token = "stored-refresh-token",
                issuedAt = LocalDateTime.now().minusDays(1),
                expiresAt = LocalDateTime.now().plusDays(1),
            )

        every { tokenProvider.parseRefreshToken("presented-refresh-token") } returns RefreshTokenClaims(member.id)
        every { refreshTokenRepository.findByMemberId(member.id) } returns storedRefreshToken

        val exception =
            shouldThrow<AuthException> {
                authService.reissueAccessToken("presented-refresh-token")
            }

        exception.errorCode shouldBe ErrorCode.INVALID_TOKEN
        verify(exactly = 0) { memberRepository.findById(any()) }
        verify(exactly = 0) { refreshTokenRepository.save(any()) }
    }

    @Test
    fun `refresh 토큰 검증에서 만료 예외가 발생하면 그대로 전파한다`() {
        every { tokenProvider.parseRefreshToken("expired-refresh-token") } throws AuthException(ErrorCode.EXPIRED_TOKEN)

        val exception =
            shouldThrow<AuthException> {
                authService.reissueAccessToken("expired-refresh-token")
            }

        exception.errorCode shouldBe ErrorCode.EXPIRED_TOKEN
        verify(exactly = 0) { refreshTokenRepository.findByMemberId(any()) }
    }

    private fun createMember(role: Role = Role.CUSTOMER): Member {
        return Member.create(
            email = Email("member@example.com"),
            passwordHash = "encoded-password",
            name = "홍길동",
            nickname = "반짝이는판다1",
            phoneNumber = "01012345678",
            role = role,
            registeredAt = LocalDateTime.now(),
        )
    }

    private fun issuedToken(token: String): IssuedToken {
        return IssuedToken(
            token = token,
            issuedAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusDays(7),
        )
    }
}
