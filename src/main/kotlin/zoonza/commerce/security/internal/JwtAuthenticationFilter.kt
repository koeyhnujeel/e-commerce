package zoonza.commerce.security.internal

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import zoonza.commerce.security.CurrentMember
import zoonza.commerce.shared.AuthErrorCode

@Component
class JwtAuthenticationFilter(
    private val jwtAccessTokenProvider: JwtAccessTokenProvider,
    private val restAuthenticationEntryPoint: RestAuthenticationEntryPoint,
) : OncePerRequestFilter() {
    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.requestURI.startsWith("/api/auth/refresh") ||
            request.requestURI.startsWith("/api/auth/logout")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val accessToken = resolveAccessToken(request)

        if (!accessToken.isNullOrBlank()) {
            try {
                authenticate(accessToken)
            } catch (e: ExpiredJwtException) {
                handleAuthenticationFailure(
                    request = request,
                    response = response,
                    authException = JwtAuthenticationException(AuthErrorCode.EXPIRED_TOKEN),
                )

                return
            } catch (e: JwtException) {
                handleAuthenticationFailure(
                    request = request,
                    response = response,
                    authException = JwtAuthenticationException(AuthErrorCode.INVALID_TOKEN),
                )

                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveAccessToken(request: HttpServletRequest): String? {
        val authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION)

        return authorizationHeader
            ?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.removePrefix(BEARER_PREFIX)
            ?.trim()
    }

    private fun authenticate(accessToken: String) {
        val authMember = jwtAccessTokenProvider.parse(accessToken)

        val authentication = CustomAuthenticationToken(
            CurrentMember(
                authMember.memberId,
                authMember.email,
                authMember.role,
            ),
            listOf(SimpleGrantedAuthority("ROLE_${authMember.role}")),
        )

        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun handleAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: JwtAuthenticationException,
    ) {
        SecurityContextHolder.clearContext()
        restAuthenticationEntryPoint.commence(request, response, authException)
    }
}
