package zoonza.commerce.like.application.port.out

import zoonza.commerce.like.domain.LikeTargetType

interface MemberLikeQueryRepository {
    fun findActiveTargetIdsByMemberIdAndTargetTypeAndTargetIdIn(
        memberId: Long,
        likeTargetType: LikeTargetType,
        targetIds: Collection<Long>,
    ): List<Long>
}