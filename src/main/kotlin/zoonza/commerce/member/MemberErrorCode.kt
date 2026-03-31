package zoonza.commerce.member

import zoonza.commerce.shared.ErrorDescriptor
import zoonza.commerce.shared.ErrorStatus

enum class MemberErrorCode(
    override val status: ErrorStatus,
    override val message: String,
) : ErrorDescriptor {
    DUPLICATE_EMAIL(ErrorStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE_NUMBER(ErrorStatus.CONFLICT, "이미 사용 중인 휴대폰 번호입니다."),
    MEMBER_NOT_FOUND(ErrorStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    MEMBER_ADDRESS_NOT_FOUND(ErrorStatus.NOT_FOUND, "배송지를 찾을 수 없습니다.");

    override val code: String
        get() = status.name
}
