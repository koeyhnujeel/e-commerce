package zoonza.commerce.member.port.out

import zoonza.commerce.common.Email
import zoonza.commerce.member.Member

interface MemberRepository {
    fun existsByEmail(email: Email): Boolean

    fun existsByPhoneNumber(phoneNumber: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun findByEmail(email: Email): Member?

    fun findById(id: Long): Member?

    fun save(member: Member): Member
}
