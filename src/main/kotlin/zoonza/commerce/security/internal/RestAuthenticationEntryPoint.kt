package zoonza.commerce.security.internal

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import zoonza.commerce.shared.AuthErrorCode
import zoonza.commerce.support.web.ApiResponse
import zoonza.commerce.support.web.ErrorResponse
import java.nio.charset.StandardCharsets

@Component
class RestAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    companion object {
        private const val WWW_AUTHENTICATE_VALUE = "Bearer"
    }

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val jwtAuthenticationException = authException as? JwtAuthenticationException
        val errorCode = jwtAuthenticationException?.errorCode ?: AuthErrorCode.UNAUTHORIZED
        val message = jwtAuthenticationException?.message ?: errorCode.message

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, WWW_AUTHENTICATE_VALUE)

        objectMapper.writeValue(
            response.outputStream,
            ApiResponse.error<Nothing>(ErrorResponse.of(errorCode, message)),
        )
    }
}
