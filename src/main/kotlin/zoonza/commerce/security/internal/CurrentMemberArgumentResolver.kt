package zoonza.commerce.security.internal

import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import zoonza.commerce.security.CurrentMember
import zoonza.commerce.security.CurrentMemberInfo
import zoonza.commerce.shared.AuthException
import zoonza.commerce.shared.AuthErrorCode

@Component
class CurrentMemberArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentMember::class.java) &&
            parameter.parameterType == CurrentMemberInfo::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AuthException(AuthErrorCode.UNAUTHORIZED)

        return authentication.principal as? CurrentMemberInfo
            ?: throw AuthException(AuthErrorCode.UNAUTHORIZED)
    }
}