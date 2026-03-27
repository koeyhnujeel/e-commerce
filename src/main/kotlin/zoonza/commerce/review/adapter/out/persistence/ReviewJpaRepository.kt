package zoonza.commerce.review.adapter.out.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewJpaRepository : JpaRepository<ReviewJpaEntity, Long> {
    fun findByMemberIdAndProductId(
        memberId: Long,
        productId: Long,
    ): ReviewJpaEntity?

    fun findByMemberIdAndProductIdAndDeletedAtIsNull(
        memberId: Long,
        productId: Long,
    ): ReviewJpaEntity?

    fun findByProductIdAndDeletedAtIsNull(
        productId: Long,
        pageable: Pageable,
    ): Page<ReviewJpaEntity>
}
