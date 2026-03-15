package zoonza.commerce.like.port.out

import zoonza.commerce.like.LikeTargetType

interface LikeTargetReader {
    fun exists(
        targetType: LikeTargetType,
        targetId: Long,
    ): Boolean
}
