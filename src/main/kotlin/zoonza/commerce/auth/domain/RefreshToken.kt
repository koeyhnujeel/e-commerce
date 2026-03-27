package zoonza.commerce.auth.domain

import java.time.LocalDateTime

class RefreshToken(
    val id: Long = 0,
    val memberId: Long,
    var token: String,
    var issuedAt: LocalDateTime,
    var expiresAt: LocalDateTime,
) {
    companion object {
        fun create(
            memberId: Long,
            token: String,
            issuedAt: LocalDateTime,
            expiresAt: LocalDateTime,
        ): RefreshToken {
            return RefreshToken(
                memberId = memberId,
                token = token,
                issuedAt = issuedAt,
                expiresAt = expiresAt,
            )
        }

    }

    fun rotate(
        token: String,
        issuedAt: LocalDateTime,
        expiresAt: LocalDateTime,
    ) {
        this.token = token
        this.issuedAt = issuedAt
        this.expiresAt = expiresAt
    }
}
