package zoonza.commerce.auth.dto

data class LoginCommand(
    val email: String,
    val password: String,
    val rememberMe: Boolean,
)
