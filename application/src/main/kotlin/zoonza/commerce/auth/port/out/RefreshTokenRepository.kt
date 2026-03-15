package zoonza.commerce.auth.port.out

import zoonza.commerce.auth.RefreshToken

interface RefreshTokenRepository {
    fun findByMemberId(memberId: Long): RefreshToken?

    fun save(refreshToken: RefreshToken): RefreshToken

    fun deleteByMemberId(memberId: Long)

    fun deleteByToken(token: String)
}
