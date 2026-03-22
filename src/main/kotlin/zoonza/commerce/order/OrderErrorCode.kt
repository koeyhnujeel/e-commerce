package zoonza.commerce.order

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class OrderErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    ORDER_NOT_FOUND(ErrorStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_ITEM_NOT_FOUND(ErrorStatus.NOT_FOUND, "주문상품을 찾을 수 없습니다."),
    ORDER_MODIFICATION_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, "수정할 수 없는 주문입니다."),
    ORDER_DELETION_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, "삭제할 수 없는 주문입니다."),
    ORDER_ITEM_PURCHASE_CONFIRM_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, "구매 확정할 수 없는 주문상품입니다.");

    override val code: String
        get() = status.name
}
