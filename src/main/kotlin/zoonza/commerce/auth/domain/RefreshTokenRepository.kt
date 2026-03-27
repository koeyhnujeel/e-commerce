package zoonza.commerce.auth.domain

interface RefreshTokenRepository {
    fun findByMemberId(memberId: Long): RefreshToken?

    fun findByToken(token: String): RefreshToken?

    fun save(refreshToken: RefreshToken): RefreshToken

    fun deleteByMemberId(memberId: Long)

    fun deleteByToken(token: String)
}