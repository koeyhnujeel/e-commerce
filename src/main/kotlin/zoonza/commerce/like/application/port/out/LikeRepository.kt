package zoonza.commerce.like.application.port.out

import zoonza.commerce.like.domain.MemberLike
import zoonza.commerce.like.domain.LikeTargetType

interface LikeRepository {
    fun findByMemberIdAndTargetId(
        memberId: Long,
        targetId: Long,
        targetType: LikeTargetType,
    ): MemberLike?

    fun countByTargetIds(
        targetIds: Collection<Long>,
        targetType: LikeTargetType,
    ): Map<Long, Long>

    fun findActiveTargetIds(
        memberId: Long,
        targetIds: Collection<Long>,
        targetType: LikeTargetType,
    ): Set<Long>

    fun save(memberLike: MemberLike): MemberLike
}
