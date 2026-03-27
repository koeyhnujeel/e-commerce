package zoonza.commerce.auth.adapter.out.persistence

import org.springframework.stereotype.Repository
import zoonza.commerce.auth.domain.RefreshToken
import zoonza.commerce.auth.domain.RefreshTokenRepository

@Repository
class RefreshTokenRepositoryAdapter(
    private val refreshTokenJpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenRepository {
    override fun findByMemberId(memberId: Long): RefreshToken? {
        return refreshTokenJpaRepository.findByMemberId(memberId)?.toDomain()
    }

    override fun findByToken(token: String): RefreshToken? {
        return refreshTokenJpaRepository.findByToken(token)?.toDomain()
    }

    override fun save(refreshToken: RefreshToken): RefreshToken {
        return refreshTokenJpaRepository.save(RefreshTokenJpaEntity.from(refreshToken)).toDomain()
    }

    override fun deleteByMemberId(memberId: Long) {
        refreshTokenJpaRepository.deleteByMemberId(memberId)
    }

    override fun deleteByToken(token: String) {
        refreshTokenJpaRepository.deleteByToken(token)
    }
}
