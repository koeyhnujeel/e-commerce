package zoonza.commerce.auth.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenJpaEntity, Long> {
    fun findByMemberId(memberId: Long): RefreshTokenJpaEntity?

    fun findByToken(token: String): RefreshTokenJpaEntity?

    fun deleteByMemberId(memberId: Long)

    fun deleteByToken(token: String)
}
