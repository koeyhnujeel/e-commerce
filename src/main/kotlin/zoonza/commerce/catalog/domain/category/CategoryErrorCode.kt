package zoonza.commerce.catalog.domain.category

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class CategoryErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    CATEGORY_NOT_FOUND(ErrorStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    ROOT_CATEGORY_REQUIRED(ErrorStatus.BAD_REQUEST, "루트 카테고리만 하위 카테고리를 조회할 수 있습니다.");

    override val code: String
        get() = status.name
}
