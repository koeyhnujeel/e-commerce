package zoonza.commerce.like.adapter.out.persistence

import zoonza.commerce.like.domain.LikeTargetType

interface MemberLikeQueryRepository {
    fun findActiveTargetIdsByMemberIdAndTargetTypeAndTargetIdIn(
        memberId: Long,
        targetType: LikeTargetType,
        targetIds: Collection<Long>,
    ): List<Long>
}
