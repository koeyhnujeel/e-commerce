package zoonza.commerce.auth.application.port.`in`

import zoonza.commerce.auth.application.dto.LoginCommand
import zoonza.commerce.auth.application.dto.LoginResult
import zoonza.commerce.auth.application.dto.ReissueTokenResult

interface AuthService {
    fun login(command: LoginCommand): LoginResult

    fun refresh(refreshToken: String): ReissueTokenResult

    fun logout(refreshToken: String?)
}
