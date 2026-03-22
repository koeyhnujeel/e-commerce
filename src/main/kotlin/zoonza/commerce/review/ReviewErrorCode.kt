package zoonza.commerce.review

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class ReviewErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    REVIEW_NOT_FOUND(ErrorStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(ErrorStatus.CONFLICT, "이미 작성된 리뷰가 있습니다."),
    REVIEW_PURCHASE_REQUIRED(ErrorStatus.BAD_REQUEST, "구매 확정된 주문상품에 대해서만 리뷰를 작성할 수 있습니다.");

    override val code: String
        get() = status.name
}
