package zoonza.commerce.adapter.out.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import zoonza.commerce.auth.dto.AccessTokenClaims
import zoonza.commerce.auth.dto.IssuedToken
import zoonza.commerce.auth.dto.RefreshTokenClaims
import zoonza.commerce.auth.port.out.TokenProvider
import zoonza.commerce.exception.AuthException
import zoonza.commerce.exception.ErrorCode
import zoonza.commerce.member.Role
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    secret: String,
    @Value("\${jwt.access-expiration-ms}")
    private val accessExpirationMs: Long,
    @Value("\${jwt.refresh-expiration-ms}")
    private val refreshExpirationMs: Long,
) : TokenProvider {
    companion object {
        private const val CLAIM_EMAIL = "email"
        private const val CLAIM_ROLE = "role"
        private const val CLAIM_TYPE = "type"
        private const val ACCESS_TOKEN_TYPE = "access"
        private const val REFRESH_TOKEN_TYPE = "refresh"
    }

    private val secretKey: SecretKey = createSecretKey(secret)
    private val parser = Jwts.parser().verifyWith(secretKey).build()

    override fun generateAccessToken(
        memberId: Long,
        email: String,
        role: Role,
    ): IssuedToken {
        return generateToken(
            subject = memberId.toString(),
            expirationMs = accessExpirationMs,
            claims =
                mapOf(
                    CLAIM_EMAIL to email,
                    CLAIM_ROLE to role.name,
                    CLAIM_TYPE to ACCESS_TOKEN_TYPE,
                ),
        )
    }

    override fun generateRefreshToken(memberId: Long): IssuedToken {
        return generateToken(
            subject = memberId.toString(),
            expirationMs = refreshExpirationMs,
            claims = mapOf(CLAIM_TYPE to REFRESH_TOKEN_TYPE),
        )
    }

    override fun validateAccessToken(token: String) {
        validateToken(token, ACCESS_TOKEN_TYPE) { claims ->
            claims.subject?.toLongOrNull()
                ?: throw AuthException(ErrorCode.INVALID_TOKEN)

            claims[CLAIM_EMAIL]?.toString()
                ?: throw AuthException(ErrorCode.INVALID_TOKEN)

            claims[CLAIM_ROLE]?.toString()
                ?.let(Role::valueOf)
                ?: throw AuthException(ErrorCode.INVALID_TOKEN)
        }
    }

    override fun validateRefreshToken(token: String) {
        validateToken(token, REFRESH_TOKEN_TYPE) { claims ->
            claims.subject?.toLongOrNull()
                ?: throw AuthException(ErrorCode.INVALID_TOKEN)
        }
    }

    override fun parseAccessToken(token: String): AccessTokenClaims {
        val claims = parser.parseSignedClaims(token).payload

        return AccessTokenClaims(
            memberId = claims.subject.toLong(),
            email = claims[CLAIM_EMAIL].toString(),
            role = Role.valueOf(claims[CLAIM_ROLE].toString()),
        )
    }

    override fun parseRefreshToken(token: String): RefreshTokenClaims {
        val claims = parser.parseSignedClaims(token).payload

        return RefreshTokenClaims(memberId = claims.subject.toLong())
    }

    private fun generateToken(
        subject: String,
        expirationMs: Long,
        claims: Map<String, String>,
    ): IssuedToken {
        val issuedAt = Instant.now()
        val expiresAt = issuedAt.plusMillis(expirationMs)
        var builder =
            Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)

        claims.forEach { (name, value) ->
            builder = builder.claim(name, value)
        }

        return IssuedToken(
            token = builder.compact(),
            issuedAt = issuedAt.toLocalDateTime(),
            expiresAt = expiresAt.toLocalDateTime(),
        )
    }

    private fun validateToken(
        token: String,
        expectedType: String,
        claimsValidator: (Claims) -> Unit,
    ) {
        try {
            val claims = parser.parseSignedClaims(token).payload

            if (claims[CLAIM_TYPE]?.toString() != expectedType) {
                throw AuthException(ErrorCode.INVALID_TOKEN)
            }

            claimsValidator(claims)
        } catch (_: ExpiredJwtException) {
            throw AuthException(ErrorCode.EXPIRED_TOKEN)
        } catch (_: IllegalArgumentException) {
            throw AuthException(ErrorCode.INVALID_TOKEN)
        } catch (_: JwtException) {
            throw AuthException(ErrorCode.INVALID_TOKEN)
        }
    }

    private fun Instant.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(this, ZoneId.systemDefault())
    }

    private fun createSecretKey(secret: String): SecretKey {
        val keyBytes =
            MessageDigest
                .getInstance("SHA-256")
                .digest(secret.toByteArray(StandardCharsets.UTF_8))

        return Keys.hmacShaKeyFor(keyBytes)
    }
}
