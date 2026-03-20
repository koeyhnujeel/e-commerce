package zoonza.commerce.auth.application.dto

data class ReissueTokenResult(
    val accessToken: String,
    val refreshToken: String,
)
