package zoonza.commerce.member

import zoonza.commerce.shared.Email

interface MemberApi {
    fun authenticate(email: Email, password: String): AuthenticatedMember

    fun findById(id: Long): AuthenticatedMember?

    fun findProfileById(id: Long): MemberProfile

    fun findProfilesByIds(ids: Set<Long>): Map<Long, MemberProfile>

    fun findShippingAddress(
        memberId: Long,
        addressId: Long,
    ): MemberAddressSnapshot

    fun findDefaultShippingAddress(memberId: Long): MemberAddressSnapshot
}
