package zoonza.commerce.catalog

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class CatalogErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    PRODUCT_NOT_FOUND(ErrorStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    PRODUCT_OPTION_NOT_FOUND(ErrorStatus.NOT_FOUND, "상품 옵션을 찾을 수 없습니다.");

    override val code: String
        get() = status.name
}
