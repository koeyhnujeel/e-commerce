package zoonza.commerce.member.port.out

import zoonza.commerce.common.Email

interface MemberRepository {
    fun existsByEmail(email: Email): Boolean
}
