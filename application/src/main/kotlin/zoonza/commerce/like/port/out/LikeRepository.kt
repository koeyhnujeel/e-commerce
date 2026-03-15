package zoonza.commerce.like.port.out

import zoonza.commerce.like.Like
import zoonza.commerce.like.LikeTargetType

interface LikeRepository {
    fun findByMemberIdAndTarget(
        memberId: Long,
        targetType: LikeTargetType,
        targetId: Long,
    ): Like?

    fun save(like: Like): Like

    fun saveIfAbsent(like: Like): Like
}
