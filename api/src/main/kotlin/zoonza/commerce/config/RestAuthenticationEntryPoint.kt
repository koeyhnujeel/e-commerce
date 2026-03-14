package zoonza.commerce.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import zoonza.commerce.adapter.`in`.exception.ErrorResponse
import zoonza.commerce.adapter.`in`.response.ApiResponse
import zoonza.commerce.exception.ErrorCode

@Component
class RestAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val errorCode =
            request.getAttribute(AUTH_ERROR_CODE_ATTRIBUTE) as? ErrorCode ?: ErrorCode.UNAUTHORIZED

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        objectMapper.writeValue(
            response.outputStream,
            ApiResponse.error<Nothing>(ErrorResponse.of(errorCode)),
        )
    }

    companion object {
        const val AUTH_ERROR_CODE_ATTRIBUTE = "authErrorCode"
    }
}
