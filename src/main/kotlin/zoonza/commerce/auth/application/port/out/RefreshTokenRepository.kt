package zoonza.commerce.auth.application.port.out

import zoonza.commerce.auth.domain.RefreshToken

interface RefreshTokenRepository {
    fun findByMemberId(memberId: Long): RefreshToken?

    fun findByToken(token: String): RefreshToken?

    fun save(refreshToken: RefreshToken): RefreshToken

    fun deleteByMemberId(memberId: Long)

    fun deleteByToken(token: String)
}
