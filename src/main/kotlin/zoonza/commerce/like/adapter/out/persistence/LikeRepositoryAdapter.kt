package zoonza.commerce.like.adapter.out.persistence

import org.springframework.stereotype.Repository
import zoonza.commerce.like.domain.LikeRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike

@Repository
class LikeRepositoryAdapter(
    private val memberLikeJpaRepository: MemberLikeJpaRepository,
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

    override fun findLikedProduct(
        memberId: Long,
        targetIds: Collection<Long>,
        likeTargetType: LikeTargetType,
    ): List<Long> {
        return memberLikeJpaRepository.findActiveTargetIds(memberId, likeTargetType, targetIds)
    }

    override fun save(memberLike: MemberLike): MemberLike {
        return memberLikeJpaRepository.save(MemberLikeJpaEntity.from(memberLike)).toDomain()
    }
}
