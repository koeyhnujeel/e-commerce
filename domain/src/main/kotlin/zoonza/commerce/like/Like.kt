package zoonza.commerce.like

import java.time.LocalDateTime

class Like private constructor(
    val id: Long = 0,
    val memberId: Long,
    val targetType: LikeTargetType,
    val targetId: Long,
    var likedAt: LocalDateTime,
    var deletedAt: LocalDateTime? = null,
) {
    companion object {
        fun create(
            memberId: Long,
            targetType: LikeTargetType,
            targetId: Long,
            likedAt: LocalDateTime,
            deletedAt: LocalDateTime? = null,
            id: Long = 0,
        ): Like {
            require(id >= 0) { "좋아요 ID는 0 이상이어야 합니다." }
            require(memberId > 0) { "회원 ID는 1 이상이어야 합니다." }
            require(targetId > 0) { "좋아요 대상 ID는 1 이상이어야 합니다." }

            return Like(
                id = id,
                memberId = memberId,
                targetType = targetType,
                targetId = targetId,
                likedAt = likedAt,
                deletedAt = deletedAt,
            )
        }
    }

    fun cancel(deletedAt: LocalDateTime) {
        this.deletedAt = deletedAt
    }

    fun restore(likedAt: LocalDateTime) {
        this.likedAt = likedAt
        this.deletedAt = null
    }

    fun isDeleted(): Boolean {
        return deletedAt != null
    }
}
