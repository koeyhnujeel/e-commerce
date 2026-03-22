package zoonza.commerce.review.application.port.out

import zoonza.commerce.common.PageQuery
import zoonza.commerce.common.PageResult
import zoonza.commerce.review.domain.Review

interface ReviewRepository {
    fun findByMemberIdAndProductId(
        memberId: Long,
        productId: Long,
    ): Review?

    fun findActiveByMemberIdAndProductId(
        memberId: Long,
        productId: Long,
    ): Review?

    fun findByProductId(
        productId: Long,
        pageQuery: PageQuery,
    ): PageResult<Review>

    fun save(review: Review): Review
}
