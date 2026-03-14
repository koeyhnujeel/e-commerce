package zoonza.commerce.adapter.`in`.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import zoonza.commerce.config.RestAuthenticationEntryPoint
import zoonza.commerce.auth.port.out.TokenProvider
import zoonza.commerce.exception.AuthException
import zoonza.commerce.exception.ErrorCode

@Component
class JwtAuthenticationFilter(
    private val tokenProvider: TokenProvider,
    private val restAuthenticationEntryPoint: RestAuthenticationEntryPoint,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.requestURI == "/api/auth/refresh"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val accessToken = resolveAccessToken(request)

            if (accessToken == null) {
                filterChain.doFilter(request, response)
                return
            }

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

            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication

            filterChain.doFilter(request, response)
        } catch (e: AuthException) {
            SecurityContextHolder.clearContext()
            request.setAttribute(RestAuthenticationEntryPoint.AUTH_ERROR_CODE_ATTRIBUTE, e.errorCode)
            restAuthenticationEntryPoint.commence(
                request,
                response,
                InsufficientAuthenticationException(e.message, e),
            )
        }
    }

    private fun resolveAccessToken(request: HttpServletRequest): String? {
        val authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw AuthException(ErrorCode.INVALID_TOKEN)
        }

        return authorizationHeader.removePrefix(BEARER_PREFIX).trim().ifBlank {
            throw AuthException(ErrorCode.INVALID_TOKEN)
        }
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}
