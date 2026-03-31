package zoonza.commerce.order.domain

import zoonza.commerce.shared.Money

class OrderItem(
    val productId: Long,
    val productOptionId: Long,
    val productName: String,
    val primaryImageUrl: String?,
    val optionColor: String,
    val optionSize: String,
    val unitPrice: Money,
    val quantity: Long,
) {
    init {
        require(productId > 0) { "상품 ID는 1 이상이어야 합니다." }
        require(productOptionId > 0) { "상품 옵션 ID는 1 이상이어야 합니다." }
        require(productName.isNotBlank()) { "상품명은 비어 있을 수 없습니다." }
        require(optionColor.isNotBlank()) { "옵션 색상은 비어 있을 수 없습니다." }
        require(optionSize.isNotBlank()) { "옵션 사이즈는 비어 있을 수 없습니다." }
        require(quantity > 0) { "주문 수량은 1 이상이어야 합니다." }
    }

    fun lineAmount(): Money = unitPrice * quantity
}
