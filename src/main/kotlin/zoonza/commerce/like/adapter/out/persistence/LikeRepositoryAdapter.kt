package zoonza.commerce.like.adapter.out.persistence

import org.springframework.stereotype.Repository
import zoonza.commerce.like.application.port.out.MemberLikeQueryRepository
import zoonza.commerce.like.domain.LikeRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike

@Repository
class LikeRepositoryAdapter(
    private val memberLikeJpaRepository: MemberLikeJpaRepository,
    private val memberLikeQueryRepository: MemberLikeQueryRepository,
) : LikeRepository {
    override fun findByMemberIdAndTargetId(
        memberId: Long,
        targetId: Long,
        likeTargetType: LikeTargetType,
    ): MemberLike? {
        return memberLikeJpaRepository
            .findByMemberIdAndTargetIdAndLikeTargetType(memberId, targetId, likeTargetType)
            ?.toDomain()
    }

    override fun findActiveTargetIds(
        memberId: Long,
        targetIds: Collection<Long>,
        likeTargetType: LikeTargetType,
    ): Set<Long> {
        if (targetIds.isEmpty()) {
            return emptySet()
        }

        return memberLikeQueryRepository.findActiveTargetIdsByMemberIdAndTargetTypeAndTargetIdIn(
            memberId = memberId,
            likeTargetType = likeTargetType,
            targetIds = targetIds,
        ).toSet()
    }

    override fun save(memberLike: MemberLike): MemberLike {
        return memberLikeJpaRepository.save(MemberLikeJpaEntity.from(memberLike)).toDomain()
    }
}
