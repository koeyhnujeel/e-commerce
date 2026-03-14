package zoonza.commerce.auth.dto

data class LoginResult(
    val accessToken: String,
    val refreshToken: String?,
)
