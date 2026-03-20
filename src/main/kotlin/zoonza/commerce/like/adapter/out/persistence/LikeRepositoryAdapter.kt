package zoonza.commerce.like.adapter.out.persistence

import org.springframework.stereotype.Repository
import zoonza.commerce.like.application.port.out.LikeRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike

@Repository
class LikeRepositoryAdapter(
    private val memberLikeJpaRepository: MemberLikeJpaRepository,
) : LikeRepository {
    override fun findByMemberIdAndTargetId(
        memberId: Long,
        targetId: Long,
        targetType: LikeTargetType,
    ): MemberLike? {
        return memberLikeJpaRepository
            .findByMemberIdAndTargetIdAndTargetType(memberId, targetId, targetType)
    }

    override fun save(memberLike: MemberLike): MemberLike {
        return memberLikeJpaRepository.save(memberLike)
    }
}
