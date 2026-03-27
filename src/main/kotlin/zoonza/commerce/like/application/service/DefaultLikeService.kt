package zoonza.commerce.like.application.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.like.LikeApi
import zoonza.commerce.like.ProductLiked
import zoonza.commerce.like.ProductUnliked
import zoonza.commerce.like.application.port.`in`.LikeService
import zoonza.commerce.like.domain.LikeErrorCode
import zoonza.commerce.like.domain.LikeRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike
import zoonza.commerce.shared.BusinessException

@Service
class DefaultLikeService(
    @Lazy
    private val catalogApi: CatalogApi,
    private val likeRepository: LikeRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : LikeApi, LikeService {
    @Transactional(readOnly = true)
    override fun findLikedProductIds(
        memberId: Long,
        productIds: Collection<Long>,
    ): Set<Long> {
        return likeRepository.findActiveTargetIds(memberId, productIds, LikeTargetType.PRODUCT)
    }

    @Transactional
    override fun likeProduct(memberId: Long, targetId: Long) {
        catalogApi.validateProductExists(targetId)

        val existingLike = likeRepository.findByMemberIdAndTargetId(memberId, targetId, LikeTargetType.PRODUCT)

        if (existingLike != null) {
            existingLike.like()
            likeRepository.save(existingLike)
        } else {
            val memberLike = MemberLike.create(memberId, targetId, LikeTargetType.PRODUCT)
            likeRepository.save(memberLike)
        }

        val event = ProductLiked(targetId)

        eventPublisher.publishEvent(event)
    }

    @Transactional
    override fun unlikeProduct(memberId: Long, targetId: Long) {
        catalogApi.validateProductExists(targetId)

        val existingLike = likeRepository.findByMemberIdAndTargetId(memberId, targetId, LikeTargetType.PRODUCT)
            ?: throw BusinessException(LikeErrorCode.LIKE_NOT_FOUND)

        existingLike.unlike()
        likeRepository.save(existingLike)

        val event = ProductUnliked(targetId)

        eventPublisher.publishEvent(event)
    }
}
