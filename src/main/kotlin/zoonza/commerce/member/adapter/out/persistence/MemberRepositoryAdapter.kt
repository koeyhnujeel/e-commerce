package zoonza.commerce.member.adapter.out.persistence

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import zoonza.commerce.member.application.port.out.MemberRepository
import zoonza.commerce.member.domain.Member
import zoonza.commerce.shared.Email

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

    override fun findByEmail(email: Email): Member? {
        return memberJapRepository.findByEmailAddress(email.address)
    }

    override fun findById(id: Long): Member? {
        return memberJapRepository.findByIdOrNull(id)
    }

    override fun findAllByIds(ids: Set<Long>): List<Member> {
        if (ids.isEmpty()) {
            return emptyList()
        }

        return memberJapRepository.findAllByIdIn(ids)
    }

    override fun save(member: Member): Member {
        return memberJapRepository.save(member)
    }
}
