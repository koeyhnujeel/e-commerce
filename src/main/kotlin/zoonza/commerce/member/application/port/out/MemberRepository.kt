package zoonza.commerce.member.application.port.out

import zoonza.commerce.member.domain.Member
import zoonza.commerce.shared.Email

interface MemberRepository {
    fun existsByEmail(email: Email): Boolean

    fun existsByPhoneNumber(phoneNumber: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun findByEmail(email: Email): Member?

    fun findById(id: Long): Member?

    fun save(member: Member): Member
}
