package zoonza.commerce.like.domain

import java.time.LocalDateTime

class MemberLike(
    val id: Long = 0,
    val memberId: Long,
    val targetId: Long,
    val likeTargetType: LikeTargetType,
    var likedAt: LocalDateTime = LocalDateTime.now(),
    var deletedAt: LocalDateTime? = null,
) {
    companion object {
        fun create(
            memberId: Long,
            targetId: Long,
            likeTargetType: LikeTargetType,
        ): MemberLike {
            require(memberId > 0) { "회원 ID는 1 이상이어야 합니다." }
            require(targetId > 0) { "좋아요 대상 ID는 1 이상이어야 합니다." }

            return MemberLike(
                memberId = memberId,
                targetId = targetId,
                likeTargetType = likeTargetType,
            )
        }

    }

    fun like() {
        check(isUnliked()) { "이미 좋아요 상태입니다." }

        this.likedAt = LocalDateTime.now()
        this.deletedAt = null
    }

    fun unlike() {
        check(isLiked()) { "이미 좋아요 해제 상태입니다." }

        this.deletedAt = LocalDateTime.now()
    }

    private fun isUnliked(): Boolean {
        return this.deletedAt != null
    }

    private fun isLiked(): Boolean {
        return this.deletedAt == null
    }
}
