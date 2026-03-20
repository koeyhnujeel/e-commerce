package zoonza.commerce.like.application.port.out

import zoonza.commerce.like.domain.MemberLike
import zoonza.commerce.like.domain.LikeTargetType

interface LikeRepository {
    fun findByMemberIdAndTargetId(
        memberId: Long,
        targetId: Long,
        targetType: LikeTargetType,
    ): MemberLike?

    fun save(memberLike: MemberLike): MemberLike
}
