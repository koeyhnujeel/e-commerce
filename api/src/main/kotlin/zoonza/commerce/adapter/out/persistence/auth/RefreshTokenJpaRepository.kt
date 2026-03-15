package zoonza.commerce.adapter.out.persistence.auth

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.auth.RefreshToken

interface RefreshTokenJpaRepository : JpaRepository<RefreshToken, Long> {
    fun findByMemberId(memberId: Long): RefreshToken?

    fun deleteByMemberId(memberId: Long)

    fun deleteByToken(token: String)
}
