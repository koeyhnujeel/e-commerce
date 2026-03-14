package zoonza.commerce.auth.dto

data class ReissueTokenResult(
    val accessToken: String,
    val refreshToken: String,
)
