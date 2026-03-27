package zoonza.commerce.catalog.domain.statistic

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class ProductStatisticErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    PRODUCT_STATISTIC_NOT_FOUND(ErrorStatus.NOT_FOUND, "상품 통계 정보를 찾을 수 없습니다.");

    override val code: String
        get() = status.name
}