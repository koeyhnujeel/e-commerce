package zoonza.commerce.review.adapter.out.persistence

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import zoonza.commerce.review.application.port.out.ReviewRepository
import zoonza.commerce.review.domain.Review
import zoonza.commerce.support.pagination.PageQuery
import zoonza.commerce.support.pagination.PageResult

@Repository
class ReviewRepositoryAdapter(
    private val reviewJpaRepository: ReviewJpaRepository,
) : ReviewRepository {
    override fun findByMemberIdAndProductId(
        memberId: Long,
        productId: Long,
    ): Review? {
        return reviewJpaRepository.findByMemberIdAndProductId(memberId, productId)
    }

    override fun findActiveByMemberIdAndProductId(
        memberId: Long,
        productId: Long,
    ): Review? {
        return reviewJpaRepository.findByMemberIdAndProductIdAndDeletedAtIsNull(memberId, productId)
    }

    override fun findByProductId(
        productId: Long,
        pageQuery: PageQuery,
    ): PageResult<Review> {
        val reviewPage = reviewJpaRepository.findByProductIdAndDeletedAtIsNull(
                productId = productId,
                pageable = PageRequest.of(
                    pageQuery.page,
                    pageQuery.size,
                    Sort.by(Sort.Direction.DESC, "updatedAt"),
                ),
            )

        return PageResult(
            items = reviewPage.content,
            page = reviewPage.number,
            size = reviewPage.size,
            totalElements = reviewPage.totalElements,
            totalPages = reviewPage.totalPages,
        )
    }

    override fun save(review: Review): Review {
        return reviewJpaRepository.save(review)
    }
}
