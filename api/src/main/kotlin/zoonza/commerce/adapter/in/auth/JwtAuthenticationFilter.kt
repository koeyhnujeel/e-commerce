package zoonza.commerce.adapter.`in`.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import zoonza.commerce.auth.port.out.TokenProvider
import zoonza.commerce.config.RestAuthenticationEntryPoint
import zoonza.commerce.exception.AuthException

@Component
class JwtAuthenticationFilter(
    private val tokenProvider: TokenProvider,
    private val restAuthenticationEntryPoint: RestAuthenticationEntryPoint,
) : OncePerRequestFilter() {
    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.requestURI == "/api/auth/refresh"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val accessToken = resolveAccessToken(request)

        if (accessToken != null) {
            try {
                authenticate(accessToken)
            } catch (e: AuthException) {
                handleAuthenticationException(request, response, e)
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun authenticate(
        accessToken: String,
    ) {
        tokenProvider.validateAccessToken(accessToken)
        val claims = tokenProvider.parseAccessToken(accessToken)
        val principal =
            AuthenticatedMember(
                memberId = claims.memberId,
                email = claims.email,
                role = claims.role,
            )
        val authentication =
            UsernamePasswordAuthenticationToken(
                principal,
                null,
                listOf(SimpleGrantedAuthority("ROLE_${claims.role.name}")),
            )

        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun handleAuthenticationException(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthException,
    ) {
        SecurityContextHolder.clearContext()

        request.setAttribute(RestAuthenticationEntryPoint.AUTH_ERROR_CODE_ATTRIBUTE, exception.errorCode)

        restAuthenticationEntryPoint.commence(
            request,
            response,
            InsufficientAuthenticationException(exception.message, exception),
        )
    }

    private fun resolveAccessToken(request: HttpServletRequest): String? {
        val authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION)

        return authorizationHeader
            ?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.removePrefix(BEARER_PREFIX)
            ?.trim()
    }
}
