package zoonza.commerce.like.application.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.catalog.CatalogApi
import zoonza.commerce.like.application.dto.ProductLikeStatus
import zoonza.commerce.like.application.port.`in`.LikeService
import zoonza.commerce.like.domain.LikeErrorCode
import zoonza.commerce.like.domain.LikeRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike
import zoonza.commerce.shared.BusinessException
import zoonza.commerce.shared.ProductLiked
import zoonza.commerce.shared.ProductUnliked

@Service
class DefaultLikeService(
    private val catalogApi: CatalogApi,
    private val likeRepository: LikeRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : LikeService {
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

        eventPublisher.publishEvent(ProductLiked(targetId))
    }

    @Transactional
    override fun unlikeProduct(memberId: Long, targetId: Long) {
        catalogApi.validateProductExists(targetId)

        val existingLike = likeRepository.findByMemberIdAndTargetId(memberId, targetId, LikeTargetType.PRODUCT)
            ?: throw BusinessException(LikeErrorCode.LIKE_NOT_FOUND)

        existingLike.unlike()
        likeRepository.save(existingLike)

        eventPublisher.publishEvent(ProductUnliked(targetId))
    }

    @Transactional(readOnly = true)
    override fun getProductLikeStatuses(
        memberId: Long,
        productIds: List<Long>
    ): List<ProductLikeStatus> {
        val likedProductIds = likeRepository.findLikedProduct(memberId, productIds, LikeTargetType.PRODUCT)

        return productIds.map { productId ->
            ProductLikeStatus(productId, productId in likedProductIds)
        }
    }
}
