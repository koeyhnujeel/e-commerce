package zoonza.commerce.auth.adapter.out

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import zoonza.commerce.auth.application.dto.IssuedToken
import zoonza.commerce.auth.application.port.out.RefreshTokenProvider
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class OpaqueRefreshTokenProvider(
    @Value("\${jwt.refresh-expiration-ms}")
    private val refreshExpirationMs: Long,
) : RefreshTokenProvider {
    companion object {
        private const val TOKEN_BYTE_LENGTH = 32
    }

    private val secureRandom = SecureRandom()

    override fun issue(): IssuedToken {
        val issuedAt = Instant.now()
        val expiresAt = issuedAt.plusMillis(refreshExpirationMs)
        val tokenBytes = ByteArray(TOKEN_BYTE_LENGTH).also(secureRandom::nextBytes)

        return IssuedToken(
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes),
            issuedAt = issuedAt.toLocalDateTime(),
            expiresAt = expiresAt.toLocalDateTime(),
        )
    }

    private fun Instant.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(this, ZoneId.systemDefault())
    }
}
