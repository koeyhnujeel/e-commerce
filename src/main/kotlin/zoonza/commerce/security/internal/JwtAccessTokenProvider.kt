package zoonza.commerce.security.internal

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import zoonza.commerce.security.AccessTokenProvider
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtAccessTokenProvider(
    @Value("\${jwt.secret}")
    private val secret: String,
    @Value("\${jwt.access-expiration-ms}")
    private val accessTtlMs: Long,
) : AccessTokenProvider {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    override fun issue(memberId: Long, email: String, role: String): String {
        val now = Instant.now()

        return Jwts.builder()
            .subject(memberId.toString())
            .claim("email", email)
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(accessTtlMs)))
            .signWith(key)
            .compact()
    }

    fun parse(token: String): AuthMember {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload

            AuthMember(
                memberId = claims.subject.toLong(),
                email = claims["email"] as String,
                role = claims["role"] as String
            )
        } catch (e: JwtException) {
            throw e
        }
    }
}