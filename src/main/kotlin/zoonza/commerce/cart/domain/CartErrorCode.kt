package zoonza.commerce.cart.domain

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class CartErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    CART_ITEM_NOT_FOUND(ErrorStatus.NOT_FOUND, "장바구니 항목을 찾을 수 없습니다."),
    CONCURRENT_CART_MODIFICATION(ErrorStatus.CONFLICT, "장바구니가 동시에 변경되었습니다. 다시 시도해 주세요.");

    override val code: String
        get() = status.name
}
