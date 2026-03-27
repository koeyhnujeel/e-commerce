package zoonza.commerce.member.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface MemberJapRepository : JpaRepository<MemberJpaEntity, Long> {
    fun existsByEmailAddress(email: String): Boolean

    fun existsByPhoneNumber(phoneNumber: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun findByEmailAddress(email: String): MemberJpaEntity?

    fun findAllByIdIn(ids: Collection<Long>): List<MemberJpaEntity>
}
