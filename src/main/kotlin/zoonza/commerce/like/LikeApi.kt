package zoonza.commerce.like

interface LikeApi {
    fun findLikedProductIds(
        memberId: Long,
        productIds: Collection<Long>,
    ): Set<Long>
}
