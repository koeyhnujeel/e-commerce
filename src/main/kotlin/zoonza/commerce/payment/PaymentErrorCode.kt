package zoonza.commerce.payment

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class PaymentErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    PAYMENT_NOT_FOUND(ErrorStatus.NOT_FOUND, "결제를 찾을 수 없습니다."),
    PAYMENT_CREATION_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, "생성할 수 없는 결제입니다."),
    PAYMENT_AMOUNT_MISMATCH(ErrorStatus.BAD_REQUEST, "주문 금액과 결제 금액이 일치하지 않습니다."),
    ACTIVE_PAYMENT_ALREADY_EXISTS(ErrorStatus.CONFLICT, "이미 진행 중인 결제가 있습니다."),
    PAYMENT_CONFIRMATION_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, "확정할 수 없는 결제입니다."),
    PAYMENT_CANCELLATION_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, "취소할 수 없는 결제입니다."),
    EXTERNAL_PAYMENT_REQUEST_FAILED(ErrorStatus.BAD_GATEWAY, "외부 결제 요청 처리에 실패했습니다.");

    override val code: String
        get() = status.name
}
