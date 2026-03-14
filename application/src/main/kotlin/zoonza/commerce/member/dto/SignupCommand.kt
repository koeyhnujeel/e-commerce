package zoonza.commerce.member.dto

data class SignupCommand(
    val email: String,
    val password: String,
    val name: String,
    val phoneNumber: String,
)
