package zoonza.commerce.like.application.port.out

import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike

interface LikeRepository {
    fun findByMemberIdAndTargetId(
        memberId: Long,
        targetId: Long,
        targetType: LikeTargetType,
    ): MemberLike?

    fun findActiveTargetIds(
        memberId: Long,
        targetIds: Collection<Long>,
        targetType: LikeTargetType,
    ): Set<Long>

    fun save(memberLike: MemberLike): MemberLike
}
