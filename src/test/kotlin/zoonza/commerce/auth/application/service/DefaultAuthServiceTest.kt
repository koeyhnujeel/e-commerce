package zoonza.commerce.auth.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test
import zoonza.commerce.auth.application.dto.IssuedToken
import zoonza.commerce.auth.application.dto.LoginCommand
import zoonza.commerce.auth.application.port.out.RefreshTokenProvider
import zoonza.commerce.auth.application.port.out.RefreshTokenRepository
import zoonza.commerce.auth.domain.RefreshToken
import zoonza.commerce.member.AuthenticatedMember
import zoonza.commerce.member.MemberApi
import zoonza.commerce.security.AccessTokenProvider
import zoonza.commerce.shared.AuthException
import zoonza.commerce.shared.Email
import zoonza.commerce.shared.ErrorCode
import java.time.LocalDateTime

class DefaultAuthServiceTest {
    private val memberApi = mockk<MemberApi>()
    private val accessTokenProvider = mockk<AccessTokenProvider>()
    private val refreshTokenProvider = mockk<RefreshTokenProvider>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val authService = DefaultAuthService(
            memberApi = memberApi,
            accessTokenProvider = accessTokenProvider,
            refreshTokenProvider = refreshTokenProvider,
            refreshTokenRepository = refreshTokenRepository,
        )

    @Test
    fun `로그인하면 access 토큰과 refresh 토큰을 발급한다`() {
        val authenticatedMember = AuthenticatedMember(1L, Email("member@example.com"), "CUSTOMER")
        val savedRefreshToken = slot<RefreshToken>()
        val issuedRefreshToken = IssuedToken(
                token = "refresh-token",
                issuedAt = LocalDateTime.of(2026, 3, 21, 10, 0),
                expiresAt = LocalDateTime.of(2026, 3, 28, 10, 0),
            )

        every { memberApi.authenticate(Email("member@example.com"), "Password123!") } returns authenticatedMember
        every { accessTokenProvider.issue(1L, "member@example.com", "CUSTOMER") } returns "access-token"
        every { refreshTokenProvider.issue() } returns issuedRefreshToken
        every { refreshTokenRepository.findByMemberId(1L) } returns null
        every { refreshTokenRepository.save(capture(savedRefreshToken)) } answers { savedRefreshToken.captured }

        val result = authService.login(
                LoginCommand(
                    email = "member@example.com",
                    password = "Password123!",
                ),
            )

        result.accessToken shouldBe "access-token"
        result.refreshToken shouldBe "refresh-token"
        savedRefreshToken.captured.memberId shouldBe 1L
        savedRefreshToken.captured.token shouldBe "refresh-token"
    }

    @Test
    fun `기존 refresh 토큰이 있으면 회전시킨다`() {
        val authenticatedMember = AuthenticatedMember(1L, Email("member@example.com"), "CUSTOMER")
        val existingRefreshToken = RefreshToken.create(
                memberId = 1L,
                token = "old-token",
                issuedAt = LocalDateTime.of(2026, 3, 20, 10, 0),
                expiresAt = LocalDateTime.of(2026, 3, 27, 10, 0),
            )
        val issuedRefreshToken = IssuedToken(
                token = "new-token",
                issuedAt = LocalDateTime.of(2026, 3, 21, 10, 0),
                expiresAt = LocalDateTime.of(2026, 3, 28, 10, 0),
            )

        every { memberApi.authenticate(Email("member@example.com"), "Password123!") } returns authenticatedMember
        every { accessTokenProvider.issue(1L, "member@example.com", "CUSTOMER") } returns "access-token"
        every { refreshTokenProvider.issue() } returns issuedRefreshToken
        every { refreshTokenRepository.findByMemberId(1L) } returns existingRefreshToken
        every { refreshTokenRepository.save(existingRefreshToken) } returns existingRefreshToken

        val result = authService.login(LoginCommand("member@example.com", "Password123!"))

        result.refreshToken shouldBe "new-token"
        existingRefreshToken.token shouldBe "new-token"
        verify(exactly = 1) { refreshTokenRepository.save(existingRefreshToken) }
    }

    @Test
    fun `유효한 refresh 토큰이면 access 토큰과 refresh 토큰을 재발급한다`() {
        val storedRefreshToken =
            RefreshToken.create(
                memberId = 1L,
                token = "stored-token",
                issuedAt = LocalDateTime.now().minusDays(1),
                expiresAt = LocalDateTime.now().plusDays(1),
            )
        val member = AuthenticatedMember(1L, Email("member@example.com"), "SELLER")
        val newRefreshToken =
            IssuedToken(
                token = "rotated-token",
                issuedAt = LocalDateTime.now(),
                expiresAt = LocalDateTime.now().plusDays(7),
            )

        every { refreshTokenRepository.findByToken("stored-token") } returns storedRefreshToken
        every { memberApi.findById(1L) } returns member
        every { accessTokenProvider.issue(1L, "member@example.com", "SELLER") } returns "new-access-token"
        every { refreshTokenProvider.issue() } returns newRefreshToken
        every { refreshTokenRepository.save(storedRefreshToken) } returns storedRefreshToken

        val result = authService.refresh("stored-token")

        result.accessToken shouldBe "new-access-token"
        result.refreshToken shouldBe "rotated-token"
        storedRefreshToken.token shouldBe "rotated-token"
    }

    @Test
    fun `refresh 토큰을 찾지 못하면 재발급에 실패한다`() {
        every { refreshTokenRepository.findByToken("missing-token") } returns null

        val exception =
            shouldThrow<AuthException> {
                authService.refresh("missing-token")
            }

        exception.errorCode shouldBe ErrorCode.INVALID_TOKEN
    }

    @Test
    fun `만료된 refresh 토큰이면 삭제하고 예외를 던진다`() {
        val expiredRefreshToken =
            RefreshToken.create(
                memberId = 1L,
                token = "expired-token",
                issuedAt = LocalDateTime.now().minusDays(8),
                expiresAt = LocalDateTime.now().minusMinutes(1),
            )
        every { refreshTokenRepository.findByToken("expired-token") } returns expiredRefreshToken
        every { refreshTokenRepository.deleteByToken("expired-token") } just Runs

        val exception =
            shouldThrow<AuthException> {
                authService.refresh("expired-token")
            }

        exception.errorCode shouldBe ErrorCode.EXPIRED_TOKEN
        verify(exactly = 1) { refreshTokenRepository.deleteByToken("expired-token") }
    }

    @Test
    fun `logout은 refresh 토큰이 있을 때만 삭제를 수행한다`() {
        every { refreshTokenRepository.deleteByToken("refresh-token") } just Runs

        authService.logout("refresh-token")
        authService.logout(null)

        verify(exactly = 1) { refreshTokenRepository.deleteByToken("refresh-token") }
    }
}
