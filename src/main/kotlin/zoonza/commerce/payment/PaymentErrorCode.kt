package zoonza.commerce.payment

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class PaymentErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    PAYMENT_NOT_FOUND(ErrorStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    PAYMENT_PREPARATION_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, "결제를 준비할 수 없는 주문입니다."),
    PAYMENT_CONFIRMATION_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, "결제를 승인할 수 없습니다."),
    PAYMENT_REFUND_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, "환불할 수 없는 결제입니다."),
    PAYMENT_CALLBACK_NOT_ALLOWED(ErrorStatus.BAD_REQUEST, "유효하지 않은 결제 콜백입니다."),
    PAYMENT_AMOUNT_MISMATCH(ErrorStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다."),
    PAYMENT_PROVIDER_ERROR(ErrorStatus.BAD_GATEWAY, "결제사 처리 중 오류가 발생했습니다.");

    override val code: String
        get() = status.name
}
