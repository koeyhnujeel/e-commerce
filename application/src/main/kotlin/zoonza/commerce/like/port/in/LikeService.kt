package zoonza.commerce.like.port.`in`

import zoonza.commerce.like.LikeTargetType

interface LikeService {
    fun like(
        memberId: Long,
        targetType: LikeTargetType,
        targetId: Long,
    )

    fun cancel(
        memberId: Long,
        targetType: LikeTargetType,
        targetId: Long,
    )
}
