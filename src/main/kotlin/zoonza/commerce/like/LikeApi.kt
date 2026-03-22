package zoonza.commerce.like

interface LikeApi {
    fun countProductLikes(productIds: Collection<Long>): Map<Long, Long>

    fun findLikedProductIds(
        memberId: Long,
        productIds: Collection<Long>,
    ): Set<Long>
}
