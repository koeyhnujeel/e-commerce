package zoonza.commerce.member.application.dto

data class SignupCommand(
    val email: String,
    val password: String,
    val name: String,
    val phoneNumber: String,
) {
    companion object {
        fun of(
            email: String,
            password: String,
            name: String,
            phoneNumber: String,
        ): SignupCommand {
            return SignupCommand(
                email = email,
                password = password,
                name = name,
                phoneNumber = phoneNumber,
            )
        }
    }
}
