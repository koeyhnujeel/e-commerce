package zoonza.commerce.like.application.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import zoonza.commerce.like.LikeApi
import zoonza.commerce.like.ProductLikeCountChanged
import zoonza.commerce.like.application.port.`in`.LikeService
import zoonza.commerce.like.application.port.out.LikeRepository
import zoonza.commerce.like.domain.LikeTargetType
import zoonza.commerce.like.domain.MemberLike

@Service
class DefaultLikeService(
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
    override fun like(
        memberId: Long,
        targetId: Long,
        targetType: LikeTargetType,
    ) {
        val like = likeRepository.findByMemberIdAndTargetId(memberId, targetId, targetType)

        if (like != null) {
            val wasActive = like.isActive()
            like.restore()
            likeRepository.save(like)
            if (!wasActive) {
                publishLikeCountChanged(targetId, 1L)
            }
        } else {
            val newMemberLike = MemberLike.create(memberId, targetId, targetType)
            likeRepository.save(newMemberLike)
            publishLikeCountChanged(targetId, 1L)
        }
    }

    @Transactional
    override fun cancelLike(
        memberId: Long,
        targetId: Long,
        targetType: LikeTargetType,
    ) {
        val like = likeRepository.findByMemberIdAndTargetId(memberId, targetId, targetType)
            ?: return

        val wasActive = like.isActive()
        like.cancel()

        likeRepository.save(like)
        if (wasActive) {
            publishLikeCountChanged(targetId, -1L)
        }
    }

    private fun publishLikeCountChanged(
        productId: Long,
        delta: Long,
    ) {
        eventPublisher.publishEvent(
            ProductLikeCountChanged(
                productId = productId,
                delta = delta,
            ),
        )
    }
}
