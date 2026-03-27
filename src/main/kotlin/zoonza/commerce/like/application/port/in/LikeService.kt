package zoonza.commerce.like.application.port.`in`

import zoonza.commerce.like.application.dto.ProductLikeStatus

interface LikeService {
    fun likeProduct(memberId: Long, targetId: Long)

    fun unlikeProduct(memberId: Long, targetId: Long)

    fun getProductLikeStatuses(memberId: Long?, productIds: List<Long>): List<ProductLikeStatus>
}
