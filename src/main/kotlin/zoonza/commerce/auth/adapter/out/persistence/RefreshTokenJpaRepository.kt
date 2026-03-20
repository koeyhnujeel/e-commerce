package zoonza.commerce.auth.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.auth.domain.RefreshToken

interface RefreshTokenJpaRepository : JpaRepository<RefreshToken, Long> {
    fun findByMemberId(memberId: Long): RefreshToken?

    fun findByToken(token: String): RefreshToken?

    fun deleteByMemberId(memberId: Long)

    fun deleteByToken(token: String)
}
