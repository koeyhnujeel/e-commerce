package zoonza.commerce.adapter.out.persistence.auth

import org.springframework.stereotype.Repository
import zoonza.commerce.auth.RefreshToken
import zoonza.commerce.auth.port.out.RefreshTokenRepository

@Repository
class RefreshTokenRepositoryAdapter(
    private val refreshTokenJpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenRepository {
    override fun findByMemberId(memberId: Long): RefreshToken? {
        return refreshTokenJpaRepository.findByMemberId(memberId)
    }

    override fun save(refreshToken: RefreshToken): RefreshToken {
        return refreshTokenJpaRepository.save(refreshToken)
    }

    override fun deleteByMemberId(memberId: Long) {
        refreshTokenJpaRepository.deleteByMemberId(memberId)
    }

    override fun deleteByToken(token: String) {
        refreshTokenJpaRepository.deleteByToken(token)
    }
}
