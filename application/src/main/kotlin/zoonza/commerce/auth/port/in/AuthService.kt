package zoonza.commerce.auth.port.`in`

import zoonza.commerce.auth.dto.LoginCommand
import zoonza.commerce.auth.dto.LoginResult
import zoonza.commerce.auth.dto.ReissueTokenResult

interface AuthService {
    fun login(command: LoginCommand): LoginResult

    fun reissueAccessToken(refreshToken: String): ReissueTokenResult
}
