package zoonza.commerce.security.internal

import org.springframework.security.core.AuthenticationException
import zoonza.commerce.shared.ErrorDescriptor

class JwtAuthenticationException(
    val errorCode: ErrorDescriptor,
    override val message: String = errorCode.message,
) : AuthenticationException(message)
