package zoonza.commerce.product

class ProductOption private constructor(
    val id: Long = 0,
    val color: String,
    val size: String,
    val stockId: Long,
) {
    companion object {
        fun create(
            color: String,
            size: String,
            stockId: Long,
            id: Long = 0,
        ): ProductOption {
            return ProductOption(
                id = validateId(id),
                color = normalizeColor(color),
                size = normalizeSize(size),
                stockId = validateStockId(stockId),
            )
        }

        private fun validateId(id: Long): Long {
            require(id >= 0) { "상품 옵션 ID는 0 이상이어야 합니다." }
            return id
        }

        private fun normalizeColor(color: String): String {
            require(color.isNotBlank()) { "상품 옵션 색상은 비어 있을 수 없습니다." }
            return color.trim()
        }

        private fun normalizeSize(size: String): String {
            require(size.isNotBlank()) { "상품 옵션 사이즈는 비어 있을 수 없습니다." }
            return size.trim()
        }

        private fun validateStockId(stockId: Long): Long {
            require(stockId > 0) { "재고 ID는 1 이상이어야 합니다." }
            return stockId
        }
    }
}
