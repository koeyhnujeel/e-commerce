package zoonza.commerce.adapter.out.persistence.member

import org.springframework.stereotype.Repository
import zoonza.commerce.member.Member
import zoonza.commerce.common.Email
import zoonza.commerce.member.port.out.MemberRepository

@Repository
class MemberRepositoryAdapter(
    private val memberJapRepository: MemberJapRepository,
) : MemberRepository {
    override fun existsByEmail(email: Email): Boolean {
        return memberJapRepository.existsByEmailAddress(email.address)
    }

    override fun existsByPhoneNumber(phoneNumber: String): Boolean {
        return memberJapRepository.existsByPhoneNumber(phoneNumber)
    }

    override fun existsByNickname(nickname: String): Boolean {
        return memberJapRepository.existsByNickname(nickname)
    }

    override fun save(member: Member): Member {
        return memberJapRepository.save(member)
    }
}
