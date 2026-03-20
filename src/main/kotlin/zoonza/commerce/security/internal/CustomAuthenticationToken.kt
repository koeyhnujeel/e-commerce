package zoonza.commerce.security.internal

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import zoonza.commerce.security.CurrentMemberInfo

class CustomAuthenticationToken(
    val currentMemberInfo: CurrentMemberInfo,
    authorities: Collection<GrantedAuthority>,
) : AbstractAuthenticationToken(authorities) {

    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any?  = null

    override fun getPrincipal(): CurrentMemberInfo = currentMemberInfo
}