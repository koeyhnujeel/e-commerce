package zoonza.commerce.shared

open class BusinessException(
    val errorCode: ErrorDescriptor,
    override val message: String = errorCode.message,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
