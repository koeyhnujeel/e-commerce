package zoonza.commerce.order

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class OrderErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    ORDER_NOT_FOUND(ErrorStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_CANCELLATION_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, "취소할 수 없는 주문입니다."),
    ORDER_PAYMENT_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, "결제 처리할 수 없는 주문입니다."),
    ORDER_EXPIRATION_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, "만료 처리할 수 없는 주문입니다.");

    override val code: String
        get() = status.name
}
