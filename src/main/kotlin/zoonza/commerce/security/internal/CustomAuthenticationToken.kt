package zoonza.commerce.security.internal

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import zoonza.commerce.security.CurrentMember

class CustomAuthenticationToken(
    val currentMember: CurrentMember,
    authorities: Collection<GrantedAuthority>,
) : AbstractAuthenticationToken(authorities) {

    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any?  = null

    override fun getPrincipal(): CurrentMember = currentMember
}
