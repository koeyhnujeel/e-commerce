package zoonza.commerce.like.adapter.out.persistence

import zoonza.commerce.like.domain.LikeTargetType

interface MemberLikeQueryRepository {
    fun countActiveByTargetTypeAndTargetIdIn(
        targetType: LikeTargetType,
        targetIds: Collection<Long>,
    ): List<LikeCountRow>

    fun findActiveTargetIdsByMemberIdAndTargetTypeAndTargetIdIn(
        memberId: Long,
        targetType: LikeTargetType,
        targetIds: Collection<Long>,
    ): List<Long>
}

data class LikeCountRow(
    val targetId: Long,
    val likeCount: Long,
)
