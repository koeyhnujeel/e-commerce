package zoonza.commerce.adapter.out.persistence.member

import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.member.Member

interface MemberJapRepository : JpaRepository<Member, Long> {
    fun existsByEmailAddress(email: String): Boolean

    fun existsByPhoneNumber(phoneNumber: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun findByEmailAddress(email: String): Member?
}
