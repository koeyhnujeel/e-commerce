package zoonza.commerce.product

class ProductImage private constructor(
    val id: Long = 0,
    val imageUrl: String,
    val isPrimary: Boolean,
    val sortOrder: Int,
) {
    companion object {
        fun create(
            imageUrl: String,
            isPrimary: Boolean,
            sortOrder: Int,
            id: Long = 0,
        ): ProductImage {
            return ProductImage(
                id = validateId(id),
                imageUrl = normalizeImageUrl(imageUrl),
                isPrimary = isPrimary,
                sortOrder = validateSortOrder(sortOrder),
            )
        }

        private fun validateId(id: Long): Long {
            require(id >= 0) { "상품 이미지 ID는 0 이상이어야 합니다." }
            return id
        }

        private fun normalizeImageUrl(imageUrl: String): String {
            require(imageUrl.isNotBlank()) { "상품 이미지 URL은 비어 있을 수 없습니다." }
            return imageUrl.trim()
        }

        private fun validateSortOrder(sortOrder: Int): Int {
            require(sortOrder >= 0) { "상품 이미지 정렬 순서는 0 이상이어야 합니다." }
            return sortOrder
        }
    }
}
