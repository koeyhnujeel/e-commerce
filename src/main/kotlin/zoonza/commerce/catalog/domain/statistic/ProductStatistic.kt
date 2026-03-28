package zoonza.commerce.catalog.domain.statistic

class ProductStatistic(
    val id: Long,
    val productId: Long,
    var likeCount: Long,
) {
    companion object {
        fun create(
            id: Long = 0,
            productId: Long,
            likeCount: Long = 0L,
        ): ProductStatistic {
            require(productId > 0) { "상품 ID는 1 이상이어야 합니다." }
            require(likeCount >= 0) { "좋아요 수는 0 이상이어야 합니다." }

            return ProductStatistic(
                id = id,
                productId = productId,
                likeCount = likeCount,
            )
        }
    }

    fun incrementLikeCount() {
        this.likeCount += 1
    }

    fun decrementLikeCount() {
        check(likeCount > 0) { "좋아요 수는 0 보다 작아질 수 없습니다." }

        this.likeCount -= 1
    }
}
