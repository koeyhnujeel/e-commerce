package zoonza.commerce.security.internal

import org.springframework.security.core.AuthenticationException
import zoonza.commerce.shared.ErrorCode

class JwtAuthenticationException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message,
) : AuthenticationException(message)
