package zoonza.commerce.inventory.domain

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class InventoryErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    STOCK_NOT_FOUND(ErrorStatus.NOT_FOUND, "재고 정보를 찾을 수 없습니다."),
    DUPLICATE_STOCK(ErrorStatus.CONFLICT, "이미 생성된 재고입니다."),
    INVALID_STOCK_QUANTITY(ErrorStatus.BAD_REQUEST, "재고 수량은 0 이상이어야 하며 조작 수량은 1 이상이어야 합니다."),
    INSUFFICIENT_AVAILABLE_STOCK(ErrorStatus.CONFLICT, "가용 재고가 부족합니다."),
    STOCK_RESERVATION_NOT_FOUND(ErrorStatus.NOT_FOUND, "재고 예약 정보를 찾을 수 없습니다."),
    DUPLICATE_ACTIVE_STOCK_RESERVATION(ErrorStatus.CONFLICT, "이미 생성된 재고 예약입니다."),
    INVALID_STOCK_RESERVATION_STATUS(ErrorStatus.CONFLICT, "현재 재고 예약 상태에서는 요청을 처리할 수 없습니다."),
    ;

    override val code: String
        get() = status.name
}