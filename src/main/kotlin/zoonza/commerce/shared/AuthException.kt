package zoonza.commerce.shared

class AuthException(
    val errorCode: ErrorDescriptor,
    override val message: String = errorCode.message,
) : RuntimeException(message)
