package zoonza.commerce.auth.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.auth.application.dto.LoginCommand
import zoonza.commerce.auth.application.dto.LoginResult
import zoonza.commerce.auth.application.dto.ReissueTokenResult
import zoonza.commerce.auth.application.port.`in`.AuthService
import zoonza.commerce.auth.application.port.out.RefreshTokenProvider
import zoonza.commerce.auth.application.port.out.RefreshTokenRepository
import zoonza.commerce.auth.domain.RefreshToken
import zoonza.commerce.member.MemberApi
import zoonza.commerce.security.AccessTokenProvider
import zoonza.commerce.shared.AuthErrorCode
import zoonza.commerce.shared.AuthException
import zoonza.commerce.shared.Email
import java.time.LocalDateTime

@Service
class DefaultAuthService(
    private val memberApi: MemberApi,
    private val accessTokenProvider: AccessTokenProvider,
    private val refreshTokenProvider: RefreshTokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
) : AuthService {
    @Transactional
    override fun login(command: LoginCommand): LoginResult {
        val authenticatedMember = memberApi.authenticate(
            Email(command.email),
            command.password
        )

        val accessToken = accessTokenProvider.issue(
            authenticatedMember.id,
            authenticatedMember.email.address,
            authenticatedMember.role
        )


        val refreshToken = issueOrRotateRefreshToken(authenticatedMember.id)

        return LoginResult(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    @Transactional
    override fun refresh(refreshToken: String): ReissueTokenResult {
        val storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                ?: throw AuthException(AuthErrorCode.INVALID_TOKEN)

        if (storedRefreshToken.expiresAt.isBefore(LocalDateTime.now())) {
            refreshTokenRepository.deleteByToken(refreshToken)
            throw AuthException(AuthErrorCode.EXPIRED_TOKEN)
        }

        val member = memberApi.findById(storedRefreshToken.memberId)
                ?: throw AuthException(AuthErrorCode.INVALID_TOKEN)

        val accessToken = accessTokenProvider.issue(
            memberId = member.id,
            email = member.email.address,
            role = member.role
        )

        val rotatedRefreshToken = refreshTokenProvider.issue()

        storedRefreshToken.rotate(
            token = rotatedRefreshToken.token,
            issuedAt = rotatedRefreshToken.issuedAt,
            expiresAt = rotatedRefreshToken.expiresAt,
        )

        refreshTokenRepository.save(storedRefreshToken)

        return ReissueTokenResult(
            accessToken = accessToken,
            refreshToken = rotatedRefreshToken.token,
        )
    }

    @Transactional
    override fun logout(refreshToken: String?) {
        if (refreshToken == null) {
            return
        }

        refreshTokenRepository.deleteByToken(refreshToken)
    }

    private fun issueOrRotateRefreshToken(memberId: Long): String {
        val issuedRefreshToken = refreshTokenProvider.issue()
        val existingRefreshToken = refreshTokenRepository.findByMemberId(memberId)

        if (existingRefreshToken != null) {
            existingRefreshToken.rotate(
                token = issuedRefreshToken.token,
                issuedAt = issuedRefreshToken.issuedAt,
                expiresAt = issuedRefreshToken.expiresAt,
            )

            refreshTokenRepository.save(existingRefreshToken)

            return issuedRefreshToken.token
        }

        val refreshToken = RefreshToken.create(
            memberId = memberId,
            token = issuedRefreshToken.token,
            issuedAt = issuedRefreshToken.issuedAt,
            expiresAt = issuedRefreshToken.expiresAt,
        )

        refreshTokenRepository.save(refreshToken)

        return issuedRefreshToken.token
    }
}
