package zoonza.commerce.support.fixture

import zoonza.commerce.member.domain.Member
import zoonza.commerce.security.AccessTokenProvider

object AuthFixture {
    fun authorizationHeader(
        accessTokenProvider: AccessTokenProvider,
        member: Member,
    ): String {
        return authorizationHeader(
            accessTokenProvider = accessTokenProvider,
            memberId = member.id,
            email = member.email.address,
            role = member.role.name,
        )
    }

    fun authorizationHeader(
        accessTokenProvider: AccessTokenProvider,
        memberId: Long,
        email: String = "member$memberId@example.com",
        role: String = "CUSTOMER",
    ): String {
        val accessToken = accessTokenProvider.issue(memberId, email, role)
        return "Bearer $accessToken"
    }
}
