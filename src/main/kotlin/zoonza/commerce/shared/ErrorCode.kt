package zoonza.commerce.shared

enum class ErrorCode(
    val code: String,
    val message: String,
) {
    INVALID_INPUT_VALUE("BAD_REQUEST", "입력값이 올바르지 않습니다."),
    INVALID_CREDENTIALS("UNAUTHORIZED", "이메일 또는 비밀번호가 올바르지 않습니다."),
    DUPLICATE_EMAIL("CONFLICT", "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE_NUMBER("CONFLICT", "이미 사용 중인 휴대폰 번호입니다."),
    EMAIL_VERIFICATION_NOT_FOUND("NOT_FOUND", "이메일 인증 요청을 찾을 수 없습니다."),
    EMAIL_NOT_VERIFIED("BAD_REQUEST", "이메일 인증이 완료되지 않았습니다."),
    INVALID_VERIFICATION_CODE("BAD_REQUEST", "인증 코드가 올바르지 않습니다."),
    EXPIRED_VERIFICATION_CODE("BAD_REQUEST", "인증 코드가 만료되었습니다."),
    PRODUCT_NOT_FOUND("NOT_FOUND", "상품을 찾을 수 없습니다."),
    PRODUCT_OPTION_NOT_FOUND("NOT_FOUND", "상품 옵션을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND("NOT_FOUND", "회원을 찾을 수 없습니다."),
    ORDER_ITEM_NOT_FOUND("NOT_FOUND", "주문상품을 찾을 수 없습니다."),
    ORDER_ITEM_PURCHASE_CONFIRM_NOT_ALLOWED("BAD_REQUEST", "구매 확정할 수 없는 주문상품입니다."),
    REVIEW_NOT_FOUND("NOT_FOUND", "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS("CONFLICT", "이미 작성된 리뷰가 있습니다."),
    REVIEW_PURCHASE_REQUIRED("BAD_REQUEST", "구매 확정된 주문상품에 대해서만 리뷰를 작성할 수 있습니다."),
    INVALID_TOKEN("UNAUTHORIZED", "인증 토큰이 올바르지 않습니다."),
    EXPIRED_TOKEN("UNAUTHORIZED", "인증 토큰이 만료되었습니다."),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다."),
}
