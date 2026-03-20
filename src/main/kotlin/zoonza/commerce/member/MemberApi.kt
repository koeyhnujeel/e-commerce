package zoonza.commerce.member

import zoonza.commerce.shared.Email

interface MemberApi {
    fun authenticate(email: Email, password: String): AuthenticatedMember

    fun findById(id: Long): AuthenticatedMember?
}