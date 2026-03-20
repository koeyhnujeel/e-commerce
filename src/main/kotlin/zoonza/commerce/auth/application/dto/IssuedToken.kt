package zoonza.commerce.auth.application.dto

import java.time.LocalDateTime

data class IssuedToken(
    val token: String,
    val issuedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
)
