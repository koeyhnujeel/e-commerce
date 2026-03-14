package zoonza.commerce.auth.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.auth.RefreshToken
import zoonza.commerce.auth.dto.IssuedToken
import zoonza.commerce.auth.dto.LoginCommand
import zoonza.commerce.auth.dto.LoginResult
import zoonza.commerce.auth.dto.ReissueTokenResult
import zoonza.commerce.auth.port.`in`.AuthService
import zoonza.commerce.auth.port.out.RefreshTokenRepository
import zoonza.commerce.auth.port.out.TokenProvider
import zoonza.commerce.common.Email
import zoonza.commerce.exception.AuthException
import zoonza.commerce.exception.ErrorCode
import zoonza.commerce.member.port.out.MemberRepository
import zoonza.commerce.member.port.out.PasswordHasher
import java.time.LocalDateTime

@Service
class DefaultAuthService(
    private val memberRepository: MemberRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenProvider: TokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
) : AuthService {
    @Transactional
    override fun login(command: LoginCommand): LoginResult {
        val member =
            memberRepository.findByEmail(Email(command.email))
                ?: throw AuthException(ErrorCode.INVALID_CREDENTIALS)

        if (!passwordHasher.matches(command.password, member.passwordHash)) {
            throw AuthException(ErrorCode.INVALID_CREDENTIALS)
        }

        member.recordLogin(LocalDateTime.now())
        memberRepository.save(member)

        val accessToken =
            tokenProvider.generateAccessToken(
                memberId = member.id,
                email = member.email.address,
                role = member.role,
            )

        val refreshToken =
            if (command.rememberMe) {
                issueOrRotateRefreshToken(member.id).token
            } else {
                refreshTokenRepository.deleteByMemberId(member.id)
                null
            }

        return LoginResult(
            accessToken = accessToken.token,
            refreshToken = refreshToken,
        )
    }

    @Transactional
    override fun reissueAccessToken(refreshToken: String): ReissueTokenResult {
        val claims = tokenProvider.parseRefreshToken(refreshToken)
        val storedRefreshToken =
            refreshTokenRepository.findByMemberId(claims.memberId)
                ?: throw AuthException(ErrorCode.INVALID_TOKEN)

        if (storedRefreshToken.token != refreshToken) {
            throw AuthException(ErrorCode.INVALID_TOKEN)
        }

        val member =
            memberRepository.findById(claims.memberId)
                ?: throw AuthException(ErrorCode.INVALID_TOKEN)

        val accessToken =
            tokenProvider.generateAccessToken(
                memberId = member.id,
                email = member.email.address,
                role = member.role,
            )
        val rotatedRefreshToken = tokenProvider.generateRefreshToken(member.id)

        storedRefreshToken.rotate(
            token = rotatedRefreshToken.token,
            issuedAt = rotatedRefreshToken.issuedAt,
            expiresAt = rotatedRefreshToken.expiresAt,
        )
        refreshTokenRepository.save(storedRefreshToken)

        return ReissueTokenResult(
            accessToken = accessToken.token,
            refreshToken = rotatedRefreshToken.token,
        )
    }

    private fun issueOrRotateRefreshToken(memberId: Long): IssuedToken {
        val issuedRefreshToken = tokenProvider.generateRefreshToken(memberId)
        val existingRefreshToken = refreshTokenRepository.findByMemberId(memberId)

        if (existingRefreshToken != null) {
            existingRefreshToken.rotate(
                token = issuedRefreshToken.token,
                issuedAt = issuedRefreshToken.issuedAt,
                expiresAt = issuedRefreshToken.expiresAt,
            )
            refreshTokenRepository.save(existingRefreshToken)
            return issuedRefreshToken
        }

        refreshTokenRepository.save(
            RefreshToken.issue(
                memberId = memberId,
                token = issuedRefreshToken.token,
                issuedAt = issuedRefreshToken.issuedAt,
                expiresAt = issuedRefreshToken.expiresAt,
            ),
        )

        return issuedRefreshToken
    }
}
