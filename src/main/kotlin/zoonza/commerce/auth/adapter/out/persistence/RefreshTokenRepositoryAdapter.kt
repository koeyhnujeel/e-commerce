package zoonza.commerce.auth.adapter.out.persistence

import org.springframework.stereotype.Repository
import zoonza.commerce.auth.application.port.out.RefreshTokenRepository
import zoonza.commerce.auth.domain.RefreshToken

@Repository
class RefreshTokenRepositoryAdapter(
    private val refreshTokenJpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenRepository {
    override fun findByMemberId(memberId: Long): RefreshToken? {
        return refreshTokenJpaRepository.findByMemberId(memberId)
    }

    override fun findByToken(token: String): RefreshToken? {
        return refreshTokenJpaRepository.findByToken(token)
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
