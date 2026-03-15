package zoonza.commerce.auth.port.out

import zoonza.commerce.auth.dto.AccessTokenClaims
import zoonza.commerce.auth.dto.IssuedToken
import zoonza.commerce.auth.dto.RefreshTokenClaims
import zoonza.commerce.member.Role

interface TokenProvider {
    fun generateAccessToken(
        memberId: Long,
        email: String,
        role: Role,
    ): IssuedToken

    fun generateRefreshToken(memberId: Long): IssuedToken

    fun validateAccessToken(token: String)

    fun validateRefreshToken(token: String)

    fun parseAccessToken(token: String): AccessTokenClaims

    fun parseRefreshToken(token: String): RefreshTokenClaims
}
