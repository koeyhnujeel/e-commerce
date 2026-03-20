package zoonza.commerce.like.application.port.`in`

import zoonza.commerce.like.domain.LikeTargetType

interface LikeService {
    fun like(
        memberId: Long,
        targetId: Long,
        targetType: LikeTargetType,
    )

    fun cancelLike(
        memberId: Long,
        targetId: Long,
        targetType: LikeTargetType,
    )
}
