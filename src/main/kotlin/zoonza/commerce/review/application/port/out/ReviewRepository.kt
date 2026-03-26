package zoonza.commerce.review.application.port.out

import zoonza.commerce.review.domain.Review
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult

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
