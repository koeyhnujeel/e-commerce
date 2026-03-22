package zoonza.commerce.review.adapter.out.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import zoonza.commerce.review.domain.Review

interface ReviewJpaRepository : JpaRepository<Review, Long> {
    fun findByMemberIdAndProductId(
        memberId: Long,
        productId: Long,
    ): Review?

    fun findByMemberIdAndProductIdAndDeletedAtIsNull(
        memberId: Long,
        productId: Long,
    ): Review?

    fun findByProductIdAndDeletedAtIsNull(
        productId: Long,
        pageable: Pageable,
    ): Page<Review>
}
