package zoonza.commerce.like.domain

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class LikeErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    LIKE_NOT_FOUND(ErrorStatus.NOT_FOUND, "좋아요 정보를 찾을 수 없습니다.");

    override val code: String
        get() = status.name
}
