package zoonza.commerce.auth.application.port.out

import zoonza.commerce.auth.application.dto.IssuedToken

interface RefreshTokenProvider {
    fun issue(): IssuedToken
}
